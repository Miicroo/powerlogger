package se.chalmers.powerlogger;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

/**
 * Singleton class for logging data.
 * Saving logged data could be done manually by calling log(),
 * or automatically when adding more data (as long as enough time
 * has passed since the file was saved).
 * 
 * @author Magnus Larsson
 */
public class FileLogger {
	private static final String defaultLogfile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/powerlogger.log",
								LOG_MESSAGE = "PowerLogger";
	private static final long TIME_DIFF_MS = 1000;
	
	private static FileLogger logger = null;
	
	private String logFilename;
	private ArrayList<Data> theData;
	private long lastUpdate;
	
	/*
	 * Creates a new FileLogger from the given settings and context.
	 * 
	 * @param settings The settings to find filename in.
	 * @param c Context to retrieve string values from.
	 */
	private FileLogger(SharedPreferences settings, Context c) {
		logFilename = settings.getString(c.getString(R.string.pref_log_filename_key), defaultLogfile);
		theData = new ArrayList<Data>();
		lastUpdate = 0;		
	}
	
	/**
	 * Returns the shared instance of FileLogger.
	 * 
	 * @param settings The settings to find filename in.
	 * @param context Context to retrieve string values from.
	 * @return The shared instance of FileLogger.
	 */
	public static FileLogger getInstance(SharedPreferences settings, Context context) {
		if(logger == null) {
			logger = new FileLogger(settings, context);
		}
		
		return logger;
	}
	
	/**
	 * Saves the logged data to file.
	 * Saved values are released to be garbage collected. 
	 */
	public synchronized void log() {
		try {				
			FileOutputStream logFile = new FileOutputStream(logFilename, true);
			DataOutputStream logOutput = new DataOutputStream(logFile);
			for(Data d : theData) {
				logOutput.writeBytes(d.toString());
			}
			
			logOutput.close();
			logFile.close();		
		}
		catch (Exception ex) {
			Log.e(LOG_MESSAGE, Log.getStackTraceString(ex));			
		}
		theData.clear(); // Do not write same data twice.
	}
	
	/**
	 * Adds a new logged value to the logger.
	 * 
	 * @param time Current time of log.
	 * @param mA Current mA value.
	 * @param voltage Current voltage.
	 * @param battery Current battery level in percentage (1-100).
	 * @param isCharging true if the phone is charging, otherwise false.
	 * @param processes A list of the currently running processes.
	 */
	public void add(long time, long mA, float voltage, int battery, boolean isCharging, List<RunningAppProcessInfo> processes) {
		theData.add(new Data(time, mA, voltage, battery, isCharging, processes));
		if(time-lastUpdate >= TIME_DIFF_MS) {
			log(); // Save to file if more than TIME_DIFF_MS has passed
			lastUpdate = time;
		}
	}
	
	/**
	 * Creates a header in the log file.
	 * Note that this overwrites all previous file data!
	 * 
	 * @param header The header to write to the log file.
	 */
	public void createFileHeader(String header) {
		try {
			FileOutputStream logFile = new FileOutputStream(logFilename, false);
			DataOutputStream logOutput = new DataOutputStream(logFile);
			
			logOutput.writeBytes(header+"\r\n");
			
			logOutput.close();
			logFile.close();			
		} catch (IOException e) {
			Log.e(LOG_MESSAGE, Log.getStackTraceString(e));
		}
	}
}
