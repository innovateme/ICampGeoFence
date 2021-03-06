package com.example.icampgeofence;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.icampgeofence.LocationMgr.OnDeleteFenceListener;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationClient.OnRemoveGeofencesResultListener;

public class MainActivity extends Activity {

	public static final String TAG = "ICAMP_GEOFENCE";
	
    private LocationMgr locationMgr = null;
    private FenceMgr fenceMgr;
    private ArrayAdapter<Fence> fenceListAdapter;
    
    private MovementMgr movementMgr = null;
    private MediaPlayer alarmPlayer;
    private TextToSpeech tts = null;
    private boolean soundsEnabled = true;

    private BroadcastReceiver activityReceiver = new ActivityReciever();
    private BroadcastReceiver transReceiver = new FenceReciever();

    public FenceMgr getFenceList() {
		return fenceMgr;
	}

	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FenceMgr.init(this);
        fenceMgr = FenceMgr.getDefault();
		
		ListView fenceListView = (ListView)findViewById(R.id.fence_list);

		fenceListAdapter = new FenceListAdapter(this, fenceMgr.getFences());
		fenceListView.setAdapter(fenceListAdapter);
		
		fenceListView.setOnItemLongClickListener(new OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
            	Fence selected = fenceListAdapter.getItem(pos);
            	deleteFenceWithConfirm(selected);
                return true;
            }
        });

        fenceListView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                Fence selected = fenceListAdapter.getItem(pos);
                if (selected.isTriggered()) {
                    selected.setTriggered(false);
                    fenceListAdapter.notifyDataSetChanged();

                    if (alarmPlayer.isPlaying()) {
                        alarmPlayer.pause();
                    }
                }
                else {
                	showFenceOnMap(selected);
                }
            }
        });

		locationMgr = new LocationMgr(this);
		
		movementMgr = new MovementMgr(this);

		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		if (savedInstanceState != null) {
			soundsEnabled = savedInstanceState.getBoolean("soundsEnabled");
		}
		
		alarmPlayer = MediaPlayer.create(MainActivity.this, R.raw.alarm);
		alarmPlayer.setLooping(true);
		alarmPlayer.setVolume(1.0f, 1.0f);

		tts = new TextToSpeech(this, new SpeechInitListener());

		// register to receive broadcasts for fence transitions
        IntentFilter transFilter = new IntentFilter(LocationMgr.TRANSITION_INTENT_ACTION);
        transFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(transReceiver, transFilter);

		// register to receive broadcasts for activity updates
        IntentFilter activityFilter = new IntentFilter(MovementMgr.ACTIVITY_INTENT_ACTION);
        activityFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(activityReceiver, activityFilter);
	}
	
	public void showFenceOnMap(Fence fence) {
		String geo = String.format("geo:0,0?q=%f,%f(%s)", fence.getLat(), fence.getLon(), fence.getName());
		Uri u = Uri.parse(geo);
		Intent intent = new Intent(Intent.ACTION_VIEW);
	    intent.setData(u);
	    if (intent.resolveActivity(getPackageManager()) != null) {
	    	startActivity(intent);
	    }
	}
	
	public class SpeechInitListener implements TextToSpeech.OnInitListener {
		@Override
		public void onInit(int status) {
		}
	}

	public class FenceReciever extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("MAIN_ACTIVITY", "Received geofence broadcast intent!");
			Toast.makeText(context, "Received geofence broadcast!", Toast.LENGTH_SHORT).show();

			// First check for errors
			if (LocationClient.hasError(intent)) {
				// Get the error code with a static method
				int errorCode = LocationClient.getErrorCode(intent);
				// Log the error
				Log.e("MAIN_ACTIVITY",
						"Location Services error: " +
								Integer.toString(errorCode));
			}
			else {
				// Get the type of transition (entry or exit)
				int transitionType = LocationClient.getGeofenceTransition(intent);
				// Test that a valid transition was reported
				if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER
					|| transitionType == Geofence.GEOFENCE_TRANSITION_EXIT
					|| transitionType == Geofence.GEOFENCE_TRANSITION_DWELL) {
					List<Geofence> triggerList =
							LocationClient.getTriggeringGeofences(intent);

					for (Geofence gf : triggerList) {
						Fence fence = FenceMgr.getDefault().getFenceById(gf.getRequestId());
						if (fence != null) {
							fence.setTriggered(true);
							fenceListAdapter.notifyDataSetChanged();
						}
					}
				}
				else {
					// An invalid transition was reported
					Log.e("MAIN_ACTIVITY",
							"Geofence transition error: " +
									Integer.toString(transitionType));
				}

				if (!alarmPlayer.isPlaying() && soundsEnabled) {
					alarmPlayer.start();
				}
		   }
		}
	}
	
	public class ActivityReciever extends BroadcastReceiver {
	   @Override
	    public void onReceive(Context context, Intent intent) {
			Log.d("MAIN_ACTIVITY", "Received Activity broadcast intent!");
			Toast.makeText(context, "Received Activity broadcast!", Toast.LENGTH_SHORT).show();

	        // If the incoming intent contains an update
	        if (ActivityRecognitionResult.hasResult(intent)) {
	            // Get the update
	            ActivityRecognitionResult result =
	                    ActivityRecognitionResult.extractResult(intent);
	            // Get the most probable activity
	            DetectedActivity mostProbableActivity =
	                    result.getMostProbableActivity();

	            Log.d("MAIN_ACTIVITY", "activity: " + mostProbableActivity);
	            
	            // Get the probability that this activity is the
	            // the user's actual activity
	            int confidence = mostProbableActivity.getConfidence();
	            // Get an integer describing the type of activity
	            int activityType = mostProbableActivity.getType();
	            
	            ActivityType type = ActivityType.fromTypeId(mostProbableActivity.getType());
	            if (soundsEnabled) {
	            	tts.speak("Current activity type is " + type, TextToSpeech.QUEUE_ADD, null);
	            }
	        
	        } else {
				Log.d("MAIN_ACTIVITY", "Received broadcast intent!");
				Toast.makeText(context, "Received transition broadcast!", Toast.LENGTH_SHORT).show();
	        }
	    }
	}
	
	
	
    @Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		soundsEnabled = savedInstanceState.getBoolean("soundsEnabled");
		if (movementMgr.isUpdatesInProgress() != savedInstanceState.getBoolean("moveMgrUpdates")) {
			toggleMoveMgr();
    	}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("soundsEnabled", soundsEnabled);
		outState.putBoolean("moveMgrUpdates", movementMgr.isUpdatesInProgress());
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

	private void deleteFenceWithConfirm(final Fence fence) {
		DialogInterface.OnClickListener deleteListener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				locationMgr.removeGeofence(fence, new OnDeleteFenceListener() {

					@Override
					public void onDeleteFence(String id) {
						fenceListAdapter.notifyDataSetChanged();
					}
				});
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage("Are you sure you want to delete this geofence?")
	           .setCancelable(false)
	           .setPositiveButton("Yes", deleteListener)
	           .setNegativeButton("No", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	                    dialog.cancel();
	               }
	           });
	    AlertDialog alert = builder.create();
	    alert.show();
	}
	
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
	public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem moveMgrItem = menu.findItem(R.id.action_toggle_movemgr);
    	if (moveMgrItem != null) {
    		moveMgrItem.setChecked(movementMgr.isUpdatesInProgress());
    	}
        MenuItem soundItem = menu.findItem(R.id.action_toggle_sounds);
    	if (soundItem != null) {
    		soundItem.setChecked(soundsEnabled);
    	}
		return super.onPrepareOptionsMenu(menu);
	}

	public void addFenceActivity(View view) {
    	Intent intent = new Intent(this, AddFenceActivity.class);
    	startActivity(intent);
    }
    
    public void deleteAllFences(View view) {
    	fenceMgr.deleteAll();
    	fenceListAdapter.notifyDataSetChanged();
    }
    
	public void toggleMoveMgr() {
		if (soundsEnabled) {
			tts.speak("Toggling activity updates", TextToSpeech.QUEUE_ADD, null);
		}

		if (movementMgr.isUpdatesInProgress()) {
			movementMgr.stopUpdates();
		}
		else {
			movementMgr.startUpdates();
		}
	}

	public void toggleSounds() {
		soundsEnabled = !soundsEnabled;
		if (!soundsEnabled && alarmPlayer.isPlaying()) {
			alarmPlayer.pause();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_about:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
	    	Intent intent = new Intent(this, AboutActivity.class);
	    	startActivity(intent);
			return true;
		case R.id.delete_all_fences:
			if (!fenceMgr.getFences().isEmpty()) {
				locationMgr.removeAllGeofences(new OnRemoveGeofencesResultListener() {
	
					@Override
					public void onRemoveGeofencesByRequestIdsResult(int statusCode, String[] geofenceRequestIds) {
						fenceListAdapter.notifyDataSetChanged();
					}
	
					@Override
					public void onRemoveGeofencesByPendingIntentResult(int arg0, PendingIntent arg1) {
						// TODO Auto-generated method stub
					}
				});
			}
			return true;
		case R.id.load_default_fences:
			if (!fenceMgr.getFences().isEmpty()) {
				locationMgr.removeAllGeofences(new OnRemoveGeofencesResultListener() {
	
					@Override
					public void onRemoveGeofencesByRequestIdsResult(int statusCode, String[] geofenceRequestIds) {
						fenceListAdapter.notifyDataSetChanged();
						List<Fence> defaultFences = fenceMgr.readDefaultFences();
						locationMgr.addGeofences(defaultFences, new OnAddGeofencesResultListener() {
							
							@Override
							public void onAddGeofencesResult(int arg0, String[] arg1) {
								fenceListAdapter.notifyDataSetChanged();
							}
						});
					}
	
					@Override
					public void onRemoveGeofencesByPendingIntentResult(int arg0, PendingIntent arg1) {
						// TODO Auto-generated method stub
					}
				});
			}
			else {
				List<Fence> defaultFences = fenceMgr.readDefaultFences();
				locationMgr.addGeofences(defaultFences, new OnAddGeofencesResultListener() {
					
					@Override
					public void onAddGeofencesResult(int arg0, String[] arg1) {
						fenceListAdapter.notifyDataSetChanged();
					}
				});
			}
			return true;
		case R.id.action_toggle_movemgr:
			toggleMoveMgr();
			return true;
		case R.id.action_toggle_sounds:
			toggleSounds();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
        unregisterReceiver(transReceiver);
        unregisterReceiver(activityReceiver);
        tts.shutdown();
        super.onDestroy();
	}
	
	
}
