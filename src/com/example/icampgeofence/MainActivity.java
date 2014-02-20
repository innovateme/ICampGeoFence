package com.example.icampgeofence;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private LocationMgr locationMgr = null;
    private FenceMgr fenceMgr;
    private ArrayAdapter<Fence> fenceListAdapter;
    
    private MovementMgr movementMgr = null;

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

		fenceListAdapter = new ArrayAdapter<Fence>(this, android.R.layout.simple_list_item_1, fenceMgr.getFences());
		fenceListView.setAdapter(fenceListAdapter);
		
		fenceListView.setOnItemLongClickListener(new OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
            	Fence selected = fenceListAdapter.getItem(pos);
            	deleteFenceWithConfirm(selected);
                return true;
            }
        });

		locationMgr = new LocationMgr(this);
		
		movementMgr = new MovementMgr(this);

		// register to receive broadcasts for fence transitions
        IntentFilter filter = new IntentFilter();
//        filter.addCategory(Intent.CATEGORY_DEFAULT);
        BroadcastReceiver receiver = new ResponseReciever();
        registerReceiver(receiver, filter);
	}
	
	public class ResponseReciever extends BroadcastReceiver {
	   @Override
	    public void onReceive(Context context, Intent intent) {
			Log.d("MAIN_ACTIVITY", "Received broadcast intent!");
			Toast.makeText(context, "Received transition broadcast!", Toast.LENGTH_SHORT).show();
	    }
	}
	
    /*
     * Called when the Activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        locationMgr.connect();
        movementMgr.connect();
    }

    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
    	movementMgr.disconnect();
    	locationMgr.disconnect();
        super.onStop();
    }

	private void deleteFenceWithConfirm(final Fence fence) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage("Are you sure you want to delete this geofence?")
	           .setCancelable(false)
	           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   locationMgr.removeGeofence(fence);
	            	   // FIXME: this will not refresh the list until the main activity is redisplayed
	            	   fenceListAdapter.notifyDataSetChanged();
	               }
	           })
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
    
	public void addFenceActivity(View view) {
    	Intent intent = new Intent(this, AddFenceActivity.class);
    	startActivity(intent);
    }
    
    public void deleteAllFences(View view) {
    	fenceMgr.deleteAll();
    	fenceListAdapter.notifyDataSetChanged();
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
		}
		return super.onOptionsItemSelected(item);
	}
}
