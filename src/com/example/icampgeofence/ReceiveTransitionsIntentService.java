package com.example.icampgeofence;

import android.app.IntentService;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;

public class ReceiveTransitionsIntentService extends IntentService {

    public ReceiveTransitionsIntentService() {
		super("ReceiveTransitionsIntentService");
	}

	private MediaPlayer player;
    private static final String TAG = "ReceiveTransitionsIntentService";

	@Override
	protected void onHandleIntent(Intent intent) {
//	    if (ActivityRecognitionResult.hasResult(intent)) {
//	    ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
//	    // Put your application specific logic here (i.e. result.getMostProbableActivity())
//}
		
		Log.d(TAG, "onHandleIntent running - received geofence intent!");
		player = MediaPlayer.create(this, R.raw.ziegengatter);
		player.setLooping(false); // Set looping
	}
}
