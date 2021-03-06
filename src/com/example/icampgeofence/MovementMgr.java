package com.example.icampgeofence;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;

public class MovementMgr implements
	GooglePlayServicesClient.ConnectionCallbacks,
	GooglePlayServicesClient.OnConnectionFailedListener {

	public static final String ACTIVITY_INTENT_ACTION = "activity_update";

	/*
	 * Define a request code to send to Google Play services
	 * This code is returned in Activity.onActivityResult
	 */
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private final static int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;
	private final static int DETECTION_INTERVAL_MILLISECONDS = 30000;

	private final Activity parentActivity;
	private final ActivityRecognitionClient activityClient;

    // Stores the PendingIntent used to request activity monitoring
    private final PendingIntent activityRequestIntent;

    // Flag that indicates if a connection is underway.
    private boolean updatesInProgress = false;
    
    public enum RequestType {
    	START,
    	STOP
    }

    private RequestType requestType = null;
    		
	public MovementMgr(Activity parent) {
			parentActivity = parent;

			/*
			 * Create a new activity client, using the enclosing class to
			 * handle callbacks.
			 */
			activityClient = new ActivityRecognitionClient(parentActivity, this, this);
			
			activityRequestIntent = getTransitionPendingIntent();
	}

	public ActivityRecognitionClient getClient() {
			return activityClient;
	}
	
	public boolean isUpdatesInProgress() {
		return updatesInProgress;
	}
	
	protected void connect() {
		try {
			activityClient.connect();
		}
		catch (Exception e) {
			Log.e("MovementMgr could not connect.", e.getMessage(), e);
		}
	}

	protected void disconnect() {
		activityClient.disconnect();
	}

	protected void startUpdates() {
		if (requestType == null && !updatesInProgress ) {
			requestType = RequestType.START;
			connect();
		}
	}

	protected void stopUpdates() {
		if (requestType == null && updatesInProgress) {
			requestType = RequestType.STOP;
			connect();
		}
	}

	/*
	 * Called by Location Services when the request to connect the
	 * client finishes successfully. At this point, you can
	 * request the current location or start periodic updates
	 */
	@Override
	public void onConnected(Bundle dataBundle) {
		if (requestType == RequestType.STOP) {
			activityClient.removeActivityUpdates(activityRequestIntent);
			updatesInProgress = false;

			// Display the request status
			Toast.makeText(parentActivity, "Activity Updates stopped", Toast.LENGTH_SHORT).show();
		}
		else {
			activityClient.requestActivityUpdates(DETECTION_INTERVAL_MILLISECONDS, activityRequestIntent);
	        updatesInProgress = true;
				
			// Display the connection status
			Toast.makeText(parentActivity, "Connected, Activity Updates started", Toast.LENGTH_SHORT).show();
			
			Notification noti = new Notification.Builder(parentActivity)
	        .setContentTitle("Activity Updates started")
	        .setContentText("Google Play Services is here to help")
	        .setSmallIcon(R.drawable.ic_launcher)
	//        .setLargeIcon(aBitmap)
	        .build();
	
			NotificationManager mNotificationManager = 
					(NotificationManager) parentActivity.getSystemService(Context.NOTIFICATION_SERVICE);		
			mNotificationManager.notify(0, noti);
		}

		requestType = null;
		disconnect();
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
		requestType = null;

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
        intent.setAction(ACTIVITY_INTENT_ACTION);

        return PendingIntent.getService(
                parentActivity,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
