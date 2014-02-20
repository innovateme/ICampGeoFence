package com.example.icampgeofence;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationClient.OnRemoveGeofencesResultListener;
import com.google.android.gms.location.LocationStatusCodes;

public class LocationMgr  implements
	GooglePlayServicesClient.ConnectionCallbacks,
	GooglePlayServicesClient.OnConnectionFailedListener {

	public static final String TRANSITION_INTENT_ACTION = "geofence_transition";

	/*
	 * Define a request code to send to Google Play services
	 * This code is returned in Activity.onActivityResult
	 */
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private final static int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;

	private final Activity parentActivity;
	private LocationClient locationClient = null;

    // Stores the PendingIntent used to request geofence monitoring
    private PendingIntent geofenceRequestIntent;

    // Flag that indicates if a request is underway.
    private boolean inProgress = false;

	
    public LocationMgr(Activity parent) {
		parentActivity = parent;

		/*
		 * Create a new location client, using the enclosing class to
		 * handle callbacks.
		 */
		locationClient = new LocationClient(parentActivity, this, this);
	}

	public LocationClient getClient() {
		return locationClient;
	}

	protected void connect() {
		// Connect the client.
		try {
			locationClient.connect();
		}
		catch (Exception e) {
			Log.e("LocationMgr could not connect.", e.getMessage(), e);
		}
	}

	protected void disconnect() {
		// Disconnecting the client invalidates it.
		locationClient.disconnect();
	}

	/*
	 * Called by Location Services when the request to connect the
	 * client finishes successfully. At this point, you can
	 * request the current location or start periodic updates
	 */
	@Override
	public void onConnected(Bundle dataBundle) {
		// Display the connection status
		Toast.makeText(parentActivity, "Connected", Toast.LENGTH_SHORT).show();

	}

	/*
	 * Called by Location Services if the connection to the
	 * location client drops because of an error.
	 */
	@Override
	public void onDisconnected() {
		// Display the connection status
		Toast.makeText(parentActivity, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
	}

	/*
	 * Called by Location Services if the attempt to
	 * Location Services fails.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
		 * Google Play services can resolve some errors it detects.
		 * If the error has a resolution, try sending an Intent to
		 * start a Google Play services activity that can resolve
		 * error.
		 */
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(
						parentActivity,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
			}
			catch (IntentSender.SendIntentException e) {
				// Log the error
				e.printStackTrace();
			}
		}
		else {
			/*
			 * If no resolution is available, display a dialog to the
			 * user with the error.
			 */
			showErrorDialog(connectionResult.getErrorCode());
		}
	}        

	void showErrorDialog(int code) {
		GooglePlayServicesUtil.getErrorDialog(code, parentActivity, REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
	}

    /*
     * Create a PendingIntent that triggers an IntentService in your
     * app when a geofence transition occurs.
     */
    private PendingIntent getTransitionPendingIntent() {
        // Create an explicit Intent
        Intent intent = new Intent(parentActivity, ReceiveTransitionsIntentService.class);
        intent.setAction(TRANSITION_INTENT_ACTION);
        /*
         * Return the PendingIntent
         */
        return PendingIntent.getService(
                parentActivity,
                0,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }
    
    public void addGeofence(final Fence fence) {
		// get pending intent for geofence transitions
		geofenceRequestIntent = getTransitionPendingIntent();
        // create new Geofence from Fence and add to play services
		Geofence gf = fence.asGeofence();
		List<Geofence> gfList = new ArrayList<Geofence>();
		gfList.add(gf);
		// Send a request to add the current geofences
        locationClient.addGeofences(gfList, geofenceRequestIntent, new OnAddGeofencesResultListener() {
			
			@Override
			public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
				// if successful, persist the new Fence
		        if (LocationStatusCodes.SUCCESS == statusCode) {
		    		FenceMgr.getDefault().add(fence);
		    		Toast.makeText(parentActivity, "Added new geofence named " + fence.getName(), Toast.LENGTH_SHORT).show();
		        }
		        else {
			        // If adding the geofences failed
		            /*
		             * Report errors here.
		             * You can log the error using Log.e() or update
		             * the UI.
		             */
		        }
		        // Turn off the in progress flag
		        inProgress = false;
		        
		        // disconnect the client
		        //locationClient.disconnect();
			}
		});
	}
    
    public void removeGeofence(final Fence fence) {
		List<String> gfList = new ArrayList<String>();
		gfList.add(fence.getId());
        locationClient.removeGeofences(gfList, new OnRemoveGeofencesResultListener() {
			
			@Override
			public void onRemoveGeofencesByRequestIdsResult(int statusCode, String[] geofenceRequestIds) {
				// if successful, persist the new Fence
		        if (LocationStatusCodes.SUCCESS == statusCode) {
		    		FenceMgr.getDefault().delete(fence);
		    		Toast.makeText(parentActivity, "Removed geofence named " + fence.getName(), Toast.LENGTH_SHORT).show();
		        }
		        else {
			        // If adding the geofences failed
		            /*
		             * Report errors here.
		             * You can log the error using Log.e() or update
		             * the UI.
		             */
		        }
		        // Turn off the in progress flag
		        inProgress = false;
			}
			
			@Override
			public void onRemoveGeofencesByPendingIntentResult(int arg0, PendingIntent arg1) {
				// TODO Auto-generated method stub
			}
		});
    }
}
