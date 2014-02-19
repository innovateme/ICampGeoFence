package com.example.icampgeofence;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;

public class AddFenceActivity extends Activity {

    private LocationMgr locationMgr = null;

	public void useCurrentLocation(View view) {
	    Location currentLoc = locationMgr.getClient().getLastLocation();
	    
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

	public void addFence(View view) {
		// add geofence storage and call APIs
	    String newLat = ((EditText) findViewById(R.id.new_lat)).getText().toString();
	    String newLong = ((EditText) findViewById(R.id.new_long)).getText().toString();
	    String newRadius = ((EditText) findViewById(R.id.new_radius)).getText().toString();
	    if (newLat.isEmpty() || newLong.isEmpty() || newRadius.isEmpty()) {
	        Toast.makeText(this, "Invalid geofence params", Toast.LENGTH_SHORT).show();
	        return;
	    }

	    Fence f = new Fence(
				"test",
				Double.parseDouble(newLat),
				Double.parseDouble(newLong),
				Float.parseFloat(newRadius),
				1000000, Geofence.GEOFENCE_TRANSITION_ENTER);

		FenceMgr.getDefault().add(f);
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
	}

    /*
     * Called when the Activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        locationMgr.start();
    }

    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
    	locationMgr.stop();
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
