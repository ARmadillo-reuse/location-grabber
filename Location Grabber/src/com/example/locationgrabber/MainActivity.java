package com.example.locationgrabber;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends ActionBarActivity implements LocationListener {
    private final String filename = "location_log.txt";
    private final String publicFilename = "public_location_log.txt";
    private int grabCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }
        
        grabCount = 0;
    }
    
    public void start(View view) {
        ToggleButton startButton = (ToggleButton) findViewById(R.id.start_button);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        if (startButton.isChecked()) {
            // Delete old log
            File log = new File(getFilesDir(), filename);
            log.delete();
            
            // Reset Count
            resetCount();
            
            // Turn on tracker
            EditText updateTime = (EditText) findViewById(R.id.update_time);
            EditText updateDist = (EditText) findViewById(R.id.update_dist);
            ToggleButton gpsMode = (ToggleButton) findViewById(R.id.gps_toggle);
            String locationProvider = "";
            if(gpsMode.isChecked()) {
                locationProvider = LocationManager.GPS_PROVIDER;
            } else {
                locationProvider = LocationManager.NETWORK_PROVIDER;
            }
            if(!updateTime.getText().toString().equals("") && !updateTime.getText().toString().equals("")) {
                Toast.makeText(getApplicationContext(), "Starting", Toast.LENGTH_SHORT).show();
                Long time = Long.parseLong(updateTime.getText().toString()) * 1000;
                Float dist = Float.parseFloat(updateDist.getText().toString());
                locationManager.requestLocationUpdates(locationProvider, time, dist, this);
            } else {
                startButton.setChecked(false);
                Toast.makeText(getApplicationContext(), "Update Time and Update Distance fields must be filled", Toast.LENGTH_SHORT).show();
            }
        } else {
            String email = ((EditText) findViewById(R.id.email)).getText().toString();
            if (!email.equals("")) {
                // Copy log to public file
                try {
                    FileInputStream input = openFileInput(filename);
                    FileOutputStream output = openFileOutput(publicFilename, Context.MODE_WORLD_READABLE);
                    for(int b = input.read(); b != -1; b = input.read()) {
                        output.write(b);
                    }
                    
                    // Send email
                    Uri URI = Uri.parse("file://" + getFilesDir() + "/" +  publicFilename);
                    final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                    emailIntent.setType("plain/text");
                    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                            new String[] { email });
                    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Location Log");
                    if (URI != null) {
                        emailIntent.putExtra(Intent.EXTRA_STREAM, URI);
                    }
                    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Location Log from " + DateFormat.format("MM/dd/yyyy hh:mm:ssa", new Date()));
                    this.startActivity(Intent.createChooser(emailIntent,
                            "Sending email..."));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
            // Turn off tracker
            locationManager.removeUpdates(this);
            Toast.makeText(getApplicationContext(), "Turning off", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container,
                    false);
            return rootView;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        incrementCount();
        String lat = String.valueOf(location.getLatitude());
        String lon = String.valueOf(location.getLongitude());
        String acc = String.valueOf(location.getAccuracy());
        String time = String.valueOf(DateFormat.format("MM/dd/yyyy hh:mm:ssa", location.getTime()));
        PrintWriter output;
        try {
            output = new PrintWriter(openFileOutput(filename, Context.MODE_APPEND), true);
            output.println(String.format("%s: lat: %s, lon: %s, acc: %s", time, lat, lon, acc));
            output.close();
            Log.i("Location", "Grabbed Location");
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "IOException", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
        
    }

    private void incrementCount() {
        grabCount++;
        ((TextView) findViewById(R.id.count)).setText(String.valueOf(grabCount));
    }
    
    private void resetCount() {
        grabCount = 0;
        ((TextView) findViewById(R.id.count)).setText(String.valueOf(grabCount));
    }
}
