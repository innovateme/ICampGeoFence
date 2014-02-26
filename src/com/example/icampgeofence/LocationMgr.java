package com.example.icampgeofence;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
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

    public interface OnDeleteFenceListener {
    	void onDeleteFence(String id);
    }
	
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

	public void setMockLocation(double latitude, double longitude, float accuracy) {
	    LocationManager lm = (LocationManager) parentActivity.getSystemService(Context.LOCATION_SERVICE);
	    lm.addTestProvider(LocationManager.GPS_PROVIDER,
	                        "requiresNetwork" == "",
	                        "requiresSatellite" == "",
	                        "requiresCell" == "",
	                        "hasMonetaryCost" == "",
	                        "supportsAltitude" == "",
	                        "supportsSpeed" == "",
	                        "supportsBearing" == "",
	                         android.location.Criteria.NO_REQUIREMENT,
	                         android.location.Criteria.ACCURACY_FINE);

	    Location newLocation = new Location(LocationManager.GPS_PROVIDER);

	    newLocation.setLatitude(latitude);
	    newLocation.setLongitude(longitude);
	    newLocation.setAccuracy(accuracy);

	    lm.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);

	    lm.setTestProviderStatus(LocationManager.GPS_PROVIDER,
	                             LocationProvider.AVAILABLE,
	                             null,System.currentTimeMillis());

	    lm.setTestProviderLocation(LocationManager.GPS_PROVIDER, newLocation);
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
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
    
    public void addGeofences(final List<Fence> fenceList, final OnAddGeofencesResultListener listener) {
    	List<Geofence> gfList = new ArrayList<Geofence>();
    	// create a Geofence from each Fence
    	for (Fence f : fenceList) {
    		gfList.add(f.asGeofence());
    	}
		// get pending intent for geofence transitions
		geofenceRequestIntent = getTransitionPendingIntent();
		// Send a request to add the current geofences
        locationClient.addGeofences(gfList, geofenceRequestIntent, new OnAddGeofencesResultListener() {
			
			@Override
			public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
				// if successful, persist the new Fence
		        if (LocationStatusCodes.SUCCESS == statusCode) {
		    		FenceMgr.getDefault().add(fenceList);
		    		listener.onAddGeofencesResult(statusCode, geofenceRequestIds);
		    		Toast.makeText(parentActivity, "Added " + fenceList.size() + " new geofences.", Toast.LENGTH_SHORT).show();
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
		});
    }
    
    
    public void removeAllGeofences(final OnRemoveGeofencesResultListener listener) {
    	if (!FenceMgr.getDefault().getFences().isEmpty()) {
    		removeGeofences(FenceMgr.getDefault().getFences(), listener);
    	}
    }

    
    public void removeGeofences(final List<Fence> fenceList, final OnRemoveGeofencesResultListener listener) {
    	List<String> gfList = new ArrayList<String>();
    	for (Fence f : fenceList) {
    		gfList.add(f.getId());
    	}
        locationClient.removeGeofences(gfList, new OnRemoveGeofencesResultListener() {
			
			@Override
			public void onRemoveGeofencesByRequestIdsResult(int statusCode, String[] geofenceRequestIds) {
				// if successful, persist the change
		        if (LocationStatusCodes.SUCCESS == statusCode) {
		    		FenceMgr.getDefault().delete(fenceList);
		    		listener.onRemoveGeofencesByRequestIdsResult(statusCode, geofenceRequestIds);
		    		Toast.makeText(parentActivity, "Removed " + geofenceRequestIds.length + " geofences.", Toast.LENGTH_SHORT).show();
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
		        else if (LocationStatusCodes.GEOFENCE_NOT_AVAILABLE == statusCode) {
			        // If adding the geofences failed
		        	Toast.makeText(parentActivity, "Error: Location Access turned off in Settings", Toast.LENGTH_SHORT).show();
		        }
		        else if (LocationStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES == statusCode) {
			        // If adding the geofences failed
		        	Toast.makeText(parentActivity, "Error: Geofence limit exceeded" + fence.getName() + ", Code:" + statusCode, Toast.LENGTH_SHORT).show();		        	
		        }
		        else {
			        // If adding the geofences failed
		        	Toast.makeText(parentActivity, "Error: Geofence not added", Toast.LENGTH_SHORT).show();
		        }
		        // Turn off the in progress flag
		        inProgress = false;
			}
		});
	}
    
    public void removeGeofence(final Fence fence, final OnDeleteFenceListener listener) {
		List<String> gfList = new ArrayList<String>();
		gfList.add(fence.getId());
        locationClient.removeGeofences(gfList, new OnRemoveGeofencesResultListener() {
			
			@Override
			public void onRemoveGeofencesByRequestIdsResult(int statusCode, String[] geofenceRequestIds) {
				// if successful, persist the change
		        if (LocationStatusCodes.SUCCESS == statusCode) {
		    		FenceMgr.getDefault().delete(fence);
		    		listener.onDeleteFence(fence.getId());
		    		Toast.makeText(parentActivity, "Removed geofence named " + fence.getName(), Toast.LENGTH_SHORT).show();
		        }
		        else if (LocationStatusCodes.GEOFENCE_NOT_AVAILABLE == statusCode) {
			        // If adding the geofences failed
		        	Toast.makeText(parentActivity, "Error: Location Access turned off in Settings", Toast.LENGTH_SHORT).show();
		        }
		        else {
			        // If adding the geofences failed
		        	Toast.makeText(parentActivity, "Error: Geofence not removed", Toast.LENGTH_SHORT).show();
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
