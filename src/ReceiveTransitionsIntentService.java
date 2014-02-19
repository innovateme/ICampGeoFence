import android.R;
import android.app.IntentService;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


public class ReceiveTransitionsIntentService extends IntentService {

    public ReceiveTransitionsIntentService() {
		super("ReceiveTransitionsIntentService");
		// TODO Auto-generated constructor stub
	}

	private static final String TAG = "ReceiveTransitionsIntentService";
    MediaPlayer player;

	@Override
	protected void onHandleIntent(Intent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public void onCreate() {
		Toast.makeText(this, "ReceiveTransitionsIntentService Created", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onCreate");
		player = MediaPlayer.create(this, R.raw.sound_file_1);
		player.setLooping(false); // Set looping
	}
	@Override
	public void onDestroy() {
		Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onDestroy");
		player.stop();
	}

	@Override
	public void onStart(Intent intent, int startid) {
		Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onStart");
		player.start();
	}

}
