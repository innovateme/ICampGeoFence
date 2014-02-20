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

		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(transitionIntent.getAction());
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		broadcastIntent.putExtras(transitionIntent);
		sendBroadcast(broadcastIntent);
	}
}
