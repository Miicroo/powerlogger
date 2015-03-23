package se.chalmers.powerlogger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;

/**
 * Class that represents one logged energy state.
 * Use toString() to get a Data object as a correctly formatted String.
 * 
 * @author Magnus Larsson
 */
public class Data {

	private static final String NO_DATA_TEXT = "no data";
	
	private long time;
	private Long mA;
	private float voltage;
	private int battery;
	private boolean isCharging;
	private List<ActivityManager.RunningAppProcessInfo> processes;
	
	/**
	 * Creates a new Data object from the given parameters.
	 * 
	 * @param time Time of logged state.
	 * @param mA Current mA, can be null if no current value is applicable.
	 * @param voltage Current voltage. 
	 * @param battery Current battery percentage left.
	 * @param isCharging true if device is charging, otherwise false.
	 * @param processes List of running processes. Can be null.
	 */
	public Data(long time, Long mA, float voltage, int battery, boolean isCharging, 
			List<RunningAppProcessInfo> processes) {
		this.time = time;
		this.mA = mA;
		this.voltage = voltage;
		this.battery = battery;
		this.isCharging = isCharging;
		this.processes = processes;
	}

	@Override
	public String toString() {
		String currentText = null;
		if (mA == null) {
			currentText = NO_DATA_TEXT;
		} else {
			if (mA < 0) {
				mA = mA * (-1);
			}
			
			currentText = (isCharging ? "" : "-") + mA.toString(); // Add - if not charging
		}	

		String powerText = NO_DATA_TEXT;
		
		if(mA != null) {
			float currentPower = voltage*mA/1000;
			powerText = Float.toString(currentPower);
		}
		
		// date, mA, V, W, percentage. See header in strings.xml.
		StringBuilder builder = new StringBuilder();
		builder.append(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS", Locale.US).format(new Date(time)));
		builder.append(",");
		builder.append(currentText);
		builder.append(",");
		builder.append(voltage);
		builder.append(",");
		builder.append(powerText);
		builder.append(",");
		builder.append(battery);
		
		// Log processes
		if (processes != null) {
			builder.append(",");
			for (RunningAppProcessInfo processInfo : processes) {
				builder.append(processInfo.processName);
				builder.append(";");
			}
		}

		builder.append("\r\n");
		return builder.toString();
	}
}
