package se.chalmers.powerlogger;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import se.chalmers.powerlogger.currentreader.CurrentReaderFactory;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;

public class BackgroundService extends Service {
	
	// Settings
	private long msInterval;
	private SharedPreferences settings;
	
	// Logging variables
	private static Timer timer;
	private FileLogger logger;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Init settings
		settings = getSharedPreferences(getString(R.string.shared_prefs_name), 0);
		
		msInterval = 1000;
		try {
			msInterval = Long.parseLong(settings.getString(getString(R.string.pref_interval_key), "1000"));
		}
		catch(Exception ex) {
			msInterval = 1000;
			Log.e(getString(R.string.app_name), ex.getMessage(), ex);
		}
		
		// Init logging variables
		logger = FileLogger.getInstance(settings, getApplicationContext());
		startWork();
		
		return Service.START_STICKY;
	}
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		logger.log();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		stopWork();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/*
	 * Starts logging, and keeps on logging as long as
	 * timer isn't cancelled.
	 */
	private void startWork() {
		timer = new Timer();
		timer.scheduleAtFixedRate (
			new TimerTask() {
				public void run() {
					Long mAvalue = CurrentReaderFactory.getValue(); // Get current mA.
					int batteryLevel = -1;
					float currentVoltage = 0;
					boolean isCharging = false;
					List<ActivityManager.RunningAppProcessInfo> processes = null;
					
					// Get information from battery intent.
					try {
						Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
						if (batteryIntent != null) {
							int scale = batteryIntent.getIntExtra("scale", 100);
							batteryLevel = (int)((float)batteryIntent.getIntExtra("level", 0)*100/scale);
							currentVoltage = (float)batteryIntent.getIntExtra("voltage", 0) / 1000;
						}
						isCharging = batteryIntent.getIntExtra("status", 1) == BatteryManager.BATTERY_STATUS_CHARGING;
					}
					catch (Exception ex) {
						Log.e(getString(R.string.app_name), Log.getStackTraceString(ex));
					}
					
					// Get list of running processes.
					if (settings.getBoolean(getString(R.string.pref_log_apps_key), false)) {
			            ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
			            processes = activityManager.getRunningAppProcesses();
					}

					logger.add(System.currentTimeMillis(), mAvalue, currentVoltage, batteryLevel, isCharging, processes);
				}
			}, 0, msInterval);
	}

	/*
	 * Stops the timer.
	 */
	private void stopWork() {
		if (timer != null) {
			//logger.log();
			timer.cancel();
		}
	}
}
