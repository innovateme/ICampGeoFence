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
	protected void onHandleIntent(Intent arg0) {
		Log.d(TAG, "onHandleIntent running - received geofence intent!");
		player = MediaPlayer.create(this, R.raw.ziegengatter);
		player.setLooping(false); // Set looping
	}
}
