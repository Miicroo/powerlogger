package se.chalmers.powerlogger;

import java.io.File;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.widget.Toast;

/**
 * Activity for managing settings.
 * All settings are stored in a SharedPreference file.
 * 
 * @author Magnus Larsson
 */
public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private final static String defaultLogfile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/powerlogger.log";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onBackPressed() {
		// Start main and then finish this activity.
		startActivity(new Intent(SettingsActivity.this, MainActivity.class));
		finish();
	}
	

	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,	Preference preference) {
		
		if (preference.getKey().equals(getString(R.string.clear_log))) {
			new AlertDialog.Builder(this)
					.setMessage("Are you sure you want to delete the log file?")
					.setCancelable(false)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,	int which) {
									SharedPreferences settings = getSharedPreferences(getString(R.string.shared_prefs_name), 0);
									String logFilename = settings.getString(getString(R.string.pref_log_filename_key), defaultLogfile);
									File f = new File(logFilename);
									Toast t = null;
									if (f.exists()) {
										if (f.delete()) {
											t = Toast.makeText(getApplicationContext(), "Log file deleted", Toast.LENGTH_SHORT);
											// Create headers every time file is deleted
											FileLogger.getInstance(settings, getApplicationContext()).createFileHeader(getString(R.string.header));
										} else {
											t = Toast.makeText(getApplicationContext(), "Error deleting log file", Toast.LENGTH_SHORT);
										}
									} else {
										t = Toast.makeText(getApplicationContext(),	"No log file", Toast.LENGTH_SHORT);
									}
									t.show();
								}
							})
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,	int which) {
									dialog.dismiss();
								}
							}).show();
			return true;
		}
		return false;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// Edit used settings
		SharedPreferences settings = getSharedPreferences(getApplicationContext().getString(R.string.shared_prefs_name), 0);
		SharedPreferences.Editor editor = settings.edit();
		
		String logApps = getApplicationContext().getString(R.string.pref_log_apps_key);
		if(key.equals(logApps)){
			editor.putBoolean(key, sharedPreferences.getBoolean(key, false));
		} else {
			editor.putString(key, sharedPreferences.getString(key, ""));
		}
		
		editor.commit(); // Save
	}
}
