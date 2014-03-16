package com.mavedev.battery;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
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
import android.os.IBinder;
import android.widget.RemoteViews;

public class BatteryMonitorService extends Service{

	private static final String ACTION_BATTERY_UPDATE = "com.mavedev.battery.action.UPDATE";
	private int batteryLevel = 0;
	private boolean isCharging = false;
	
	BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			LogFile.log("onReceive() " + intent.getAction());
			System.out.println("intent recieved: "+intent.getAction());
				System.out.println("battery changed");
				int currentLevel = calculateBatteryLevel(context);
					batteryLevel = currentLevel;
					updateViews(context);

		}
	};
	
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
		}
		if(isCharging){
			outerCirclePaint.setColor(Color.GREEN);
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
		
		Intent configIntent = new Intent(context, BatteryWidgetConfigure.class);
	    PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
	    views.setOnClickPendingIntent(R.id.canvas, configPendingIntent);
		appWidgetManager.updateAppWidget(componentName, views);
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		startUp(intent);
		return START_STICKY;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		startUp(intent);
	}

	private void startUp(Intent intent) {
		registerListeners();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(batteryLevelReceiver);
	}

	private void registerListeners() {

		IntentFilter batteryLevelFilter = new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(batteryLevelReceiver, batteryLevelFilter);
	}


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


}
