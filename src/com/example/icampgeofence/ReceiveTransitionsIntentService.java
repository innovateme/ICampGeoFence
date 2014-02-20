package com.example.icampgeofence;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class ReceiveTransitionsIntentService extends IntentService {

    private static final String TAG = "ReceiveTransitionsIntentService";

    public ReceiveTransitionsIntentService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent transitionIntent) {
		Log.d(TAG, "onHandleIntent running - received geofence intent!");

		// rebroadcast the intent so the UI can receive it
		sendBroadcast(transitionIntent);
	}

//	@Override
//	protected void onHandleIntent(Intent intent) {
////	    if (ActivityRecognitionResult.hasResult(intent)) {
////	    ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
////	    // Put your application specific logic here (i.e. result.getMostProbableActivity())
////}
//		
//		Log.d(TAG, "onHandleIntent running - received geofence intent!");
//		player = MediaPlayer.create(this, R.raw.ziegengatter);
//		player.setLooping(false); // Set looping
//	}

}
