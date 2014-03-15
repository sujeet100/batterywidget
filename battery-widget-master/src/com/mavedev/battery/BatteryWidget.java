package com.mavedev.battery;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.BatteryManager;
import android.os.SystemClock;
import android.widget.RemoteViews;


public class BatteryWidget extends AppWidgetProvider {
	private static final String ACTION_BATTERY_UPDATE = "com.mavedev.battery.action.UPDATE";
	private int batteryLevel = 0;
	private boolean isCharging = false;
	
	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		
		LogFile.log("onEnabled()");

		turnAlarmOnOff(context, true);
		context.startService(new Intent(context, ScreenMonitorService.class));
	}
	
	public static void turnAlarmOnOff(Context context, boolean turnOn) {
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(ACTION_BATTERY_UPDATE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		if (turnOn) { // Add extra 1 sec because sometimes ACTION_BATTERY_CHANGED is called after the first alarm
			alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, 300 * 1000, pendingIntent);
			LogFile.log("Alarm set");
		} else {
			alarmManager.cancel(pendingIntent);
			LogFile.log("Alarm disabled");
		}
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		LogFile.log("onUpdate()");

		// Sometimes when the phone is booting, onUpdate method gets called before onEnabled()
		int currentLevel = calculateBatteryLevel(context);
		if (batteryChanged(currentLevel)) {
			batteryLevel = currentLevel;
			LogFile.log("Battery changed");
		}
		updateViews(context);
	}
	
	private boolean batteryChanged(int currentLevelLeft) {
		return (batteryLevel != currentLevelLeft);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		
		LogFile.log("onReceive() " + intent.getAction());
		
		if (intent.getAction().equals(ACTION_BATTERY_UPDATE)) {
			int currentLevel = calculateBatteryLevel(context);
			if (batteryChanged(currentLevel)) {
				LogFile.log("Battery changed");
				batteryLevel = currentLevel;
				updateViews(context);
			}
		}
	}
	
	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		
		LogFile.log("onDisabled()");
		
		turnAlarmOnOff(context, false);
		context.stopService(new Intent(context, ScreenMonitorService.class));
	}
	
	private int calculateBatteryLevel(Context context) {
		LogFile.log("calculateBatteryLevel()");
		
		Intent batteryIntent = context.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		
		int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
		int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
		int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		
		isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
		
		return level * 100 / scale;
	}
	
	private void updateViews(Context context) {
		LogFile.log("updateViews()");
		//TODO: remove
		/*batteryLevel = 100;
		isCharging = false;*/
		
		
		
		Bitmap bitmap = Bitmap.createBitmap(300, 300, Config.ARGB_8888);
		
		final int circleStroke = bitmap.getHeight()/15;
		final int PADDING = circleStroke+5;

		
		//Outer circle style
		Paint outerCirclePaint = new Paint(); 
		outerCirclePaint.setAntiAlias(true);
		outerCirclePaint.setStyle(Style.STROKE);
		outerCirclePaint.setStrokeWidth(circleStroke);
		
		//outer circle color based on battery percentage
		if(batteryLevel >=30){
			outerCirclePaint.setColor(0xFF79BEDB);
		}else if(batteryLevel<30 && batteryLevel >=15 ){
			outerCirclePaint.setColor(0xFFFF9900);
		}else if(batteryLevel < 15){
			outerCirclePaint.setColor(0xFFFF0000);
		}if(isCharging){
			outerCirclePaint.setColor(Color.YELLOW);
		}
		
		
		//inner circle style
		Paint innerCirclePaint = new Paint(); 
		innerCirclePaint.setAntiAlias(true);
		innerCirclePaint.setStyle(Style.STROKE);
		innerCirclePaint.setStrokeWidth(circleStroke/2);
		innerCirclePaint.setColor(0xFFFFFFFF);
		
		
		//battery level text style
		Paint textPaint = new Paint(); 
		textPaint.setAntiAlias(true);
		textPaint.setStyle(Style.FILL_AND_STROKE);
		textPaint.setStrokeWidth(5);
		int textFontSize = batteryLevel == 100 ? bitmap.getHeight()/4:bitmap.getHeight()/3;
		textPaint.setTextSize(textFontSize);
		textPaint.setColor(Color.WHITE);
		
		Canvas canvas = new Canvas(bitmap);
		RectF box = new RectF(0+PADDING, 0+PADDING,bitmap.getWidth()-PADDING,bitmap.getHeight()-PADDING);

		//inner circle
		Path innerCircle = new Path();
		innerCircle.addArc(box, -90, 360);
		canvas.drawPath(innerCircle, innerCirclePaint);
	
		
		
		//outer circle
		float sweep = 360 * batteryLevel * 0.01f;
		Path outerCircle = new Path();
		outerCircle.addArc(box, -90, sweep);
		canvas.drawPath(outerCircle, outerCirclePaint);
	
		//battery level
		int textX =  15+PADDING + circleStroke;
		canvas.drawText(batteryLevel + "%",textX,bitmap.getHeight()/2+textFontSize/2, textPaint);

		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		//views.setTextViewText(R.id.batteryText, batteryLevel + "%.");
		views.setImageViewBitmap(R.id.canvas, bitmap);
		
		ComponentName componentName = new ComponentName(context, BatteryWidget.class);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		
		/*Intent configIntent = new Intent(context, BatteryWidgetConfigure.class);
	    PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
	    views.setOnClickPendingIntent(R.id.canvas, configPendingIntent);*/
		appWidgetManager.updateAppWidget(componentName, views);
	}

}
