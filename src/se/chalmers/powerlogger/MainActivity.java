package se.chalmers.powerlogger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Activity which handles logging.
 * No user interaction is required, logging is started and ended when
 * the app is started and killed.
 * 
 * @author Magnus Larsson
 */
public class MainActivity extends Activity {

	private Intent serviceIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		serviceIntent = new Intent(this, BackgroundService.class);
		startService(serviceIntent);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopService(serviceIntent);
		Toast.makeText(this, getString(R.string.finished), Toast.LENGTH_SHORT).show();
	}
}
