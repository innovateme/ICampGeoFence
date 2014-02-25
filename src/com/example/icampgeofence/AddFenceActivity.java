package com.example.icampgeofence;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;

public class AddFenceActivity extends Activity implements LocationListener {

    private LocationMgr locationMgr = null;
    protected LocationManager lm;
    String provider;
    
    @Override
    public void onLocationChanged(Location loc) {
//	    EditText newLat = (EditText) findViewById(R.id.new_lat);		    
//	    EditText newLong = (EditText) findViewById(R.id.new_long);
//	    newLat.setText("");
//	    newLong.setText("");
    	
        Toast.makeText(
                getBaseContext(),
                "Location changed: Lat: " + loc.getLatitude() + " Lng: "
                    + loc.getLongitude(), Toast.LENGTH_SHORT).show();
        Log.d("ADD_FENCE_ACTIVITY", "Location changed: Lat: " + loc.getLatitude() + " Lng: "
                + loc.getLongitude());
        
//	    newLat.setText(String.valueOf(loc.getLatitude()));
//	    newLong.setText(String.valueOf(loc.getLongitude()));
        
    }

    @Override
    public void onProviderDisabled(String provider) {
    	Log.d("ADD_FENCE_ACTIVITY", "onProviderDisabled");
    }

    @Override
    public void onProviderEnabled(String provider) {
    	Log.d("ADD_FENCE_ACTIVITY", "onProviderEnabled");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    	Log.d("ADD_FENCE_ACTIVITY", "onStatusChanged");
    }
    

	public void useCurrentLocation(View view) {
        // getting GPS status
		Location currentLoc = locationMgr.getClient().getLastLocation();
    	Log.d("ADD_FENCE_ACTIVITY", "Location:" + currentLoc);
	    
	    if (currentLoc != null) {
		    EditText newLat = (EditText) findViewById(R.id.new_lat);
		    newLat.setText(String.valueOf(currentLoc.getLatitude()));
		    
		    EditText newLong = (EditText) findViewById(R.id.new_long);
		    newLong.setText(String.valueOf(currentLoc.getLongitude()));
	    }
	    else {
	        Toast.makeText(this, "Current location not available",
	                Toast.LENGTH_SHORT).show();
	        return;
	    }
	}

	public void useLmCurrentLocation(View view) {
        // getting GPS status
        boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		Location currentLoc = lm.getLastKnownLocation(provider);
    	Log.d("ADD_FENCE_ACTIVITY", "Location:" + currentLoc);
		
	    if (currentLoc != null) {
		    EditText newLat = (EditText) findViewById(R.id.new_lat);
		    newLat.setText(String.valueOf(currentLoc.getLatitude()));
		    
		    EditText newLong = (EditText) findViewById(R.id.new_long);
		    newLong.setText(String.valueOf(currentLoc.getLongitude()));
	    }
	    else if (isGPSEnabled) {
	        Toast.makeText(this, "Current location not available",
	                Toast.LENGTH_SHORT).show();
	        return;
	    }
	    else {
	        Toast.makeText(this, "GPS not enabled",
	                Toast.LENGTH_SHORT).show();
	        return;
	    }
	}

	public void addFence(View view) {
		// add geofence storage and call APIs
		String newFenceName = ((EditText) findViewById(R.id.fence_name)).getText().toString();
		Spinner spinner = (Spinner) findViewById(R.id.fence_type);
		String newFenceType = spinner.getSelectedItem().toString();
	    String newLat = ((EditText) findViewById(R.id.new_lat)).getText().toString();
	    String newLong = ((EditText) findViewById(R.id.new_long)).getText().toString();
	    String newRadius = ((EditText) findViewById(R.id.new_radius)).getText().toString();
	    if (newFenceName.isEmpty() || newLat.isEmpty() || newLong.isEmpty() || newRadius.isEmpty()) {
	        Toast.makeText(this, "Invalid geofence params", Toast.LENGTH_SHORT).show();
	        return;
	    }
        
        int selectedFenceType;
        if (newFenceType.equalsIgnoreCase("Exit")) {
            selectedFenceType = Geofence.GEOFENCE_TRANSITION_EXIT;
        }
        else if (newFenceType.equalsIgnoreCase("Enter")){
            selectedFenceType = Geofence.GEOFENCE_TRANSITION_ENTER;
        }
        else if (newFenceType.equalsIgnoreCase("Enter or Exit")){
        	selectedFenceType = (Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);
        }
        else
        {
            selectedFenceType = Geofence.GEOFENCE_TRANSITION_DWELL;
        }
        		
	    Fence f = new Fence(
				newFenceName,
				Double.parseDouble(newLat),
				Double.parseDouble(newLong),
				Float.parseFloat(newRadius),
				Geofence.NEVER_EXPIRE,
				selectedFenceType);

	    locationMgr.addGeofence(f);
		NavUtils.navigateUpFromSameTask(this);
	}
	
	public void cancel(View view) {
		NavUtils.navigateUpFromSameTask(this);
	}
	
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_fence);
		// Show the Up button in the action bar.
		setupActionBar();
		
        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
		locationMgr = new LocationMgr(this);
		
	    lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    Criteria criteria = new Criteria();
	    provider = lm.getBestProvider(criteria, false);
	}

    /* Request updates at startup */
    @Override
    protected void onResume() {
      super.onResume();
      
/*      LocationListener locationListener = new LocationListener() {
    	    public void onLocationChanged(Location location) {
    	      // Called when a new location is found by the network location provider.
    	      makeUseOfNewLocation(location);
    	    }

    	    public void onStatusChanged(String provider, int status, Bundle extras) {}

    	    public void onProviderEnabled(String provider) {}

    	    public void onProviderDisabled(String provider) {}
    	  };
*/     
      lm.requestLocationUpdates(provider, 400, 1, this);
      Log.d("ADD_FENCE_ACTIVITY", "Updates started:" + provider);
    }

    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
      super.onPause();
      lm.removeUpdates(this);
      Log.d("ADD_FENCE_ACTIVITY", "Updates stopped");
    }

    /*
     * Called when the Activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        locationMgr.connect();
    }

    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
    	locationMgr.disconnect();
        super.onStop();
    }
    
    /**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_fence, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
