package com.mavedev.battery;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
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

@SuppressLint("NewApi")
public class BatteryMonitorService extends Service{

	private static final String ACTION_BATTERY_UPDATE = "com.mavedev.battery.action.UPDATE";
	private int batteryLevel = 0;
	private boolean isCharging = false;
	
	BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
				int currentLevel = calculateBatteryLevel(context);
					batteryLevel = currentLevel;
					updateViews(context);

		}
	};
	
	private int calculateBatteryLevel(Context context) {
		
		Intent batteryIntent = context.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		
		int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
		int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
		int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		
		isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
		
		return level * 100 / scale;
	}
	
	private void updateViews(Context context) {
		
		Bitmap bitmap = Bitmap.createBitmap(300, 300, Config.ARGB_8888);
		final int circleStroke = bitmap.getHeight()/20;
		final int PADDING = circleStroke*2;

		//Outer circle style
		Paint outerCirclePaint = new Paint(); 
		outerCirclePaint.setAntiAlias(true);
		outerCirclePaint.setStyle(Style.STROKE);
		outerCirclePaint.setStrokeWidth(circleStroke);
		
		//outer circle color based on battery percentage
		if(batteryLevel >=BatteryWidgetConfigure.warningLevel){
			outerCirclePaint.setColor(0xFF2EA8D9);
		}else if(batteryLevel<BatteryWidgetConfigure.warningLevel && batteryLevel >=BatteryWidgetConfigure.criticalLevel ){
			outerCirclePaint.setColor(0xFFFF9900);
		}else if(batteryLevel < BatteryWidgetConfigure.criticalLevel){
			outerCirclePaint.setColor(0xFFFF0000);
		}
		if(isCharging){
			outerCirclePaint.setColor(0xFF92CD00);
		}
		
		
		//inner circle style
		Paint innerCirclePaint = new Paint(); 
		innerCirclePaint.setAntiAlias(true);
		innerCirclePaint.setStyle(Style.STROKE);
		innerCirclePaint.setStrokeWidth(circleStroke/3);
		innerCirclePaint.setColor(0xFFFFFFFF);
		
		
		//battery level text style
		Paint textPaint = new Paint(); 
		textPaint.setAntiAlias(true);
		textPaint.setStyle(Style.FILL_AND_STROKE);
		textPaint.setStrokeWidth(bitmap.getHeight()/100);
		int textFontSize = batteryLevel == 100 ? bitmap.getHeight()/5:bitmap.getHeight()/4;
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
		int textX =  bitmap.getHeight()/15+PADDING + 2*circleStroke;
		if(batteryLevel<10){
			textX = (int) (textX * 1.5);
		}
		canvas.drawText(batteryLevel + "%",textX,bitmap.getHeight()/2+textFontSize/2, textPaint);

		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		views.setImageViewBitmap(R.id.canvas, bitmap);
		
		ComponentName componentName = new ComponentName(context, BatteryWidget.class);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		
		Intent configIntent = new Intent(context, BatteryWidgetConfigure.class);
	    PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
	    views.setOnClickPendingIntent(R.id.canvas, configPendingIntent);
		appWidgetManager.updateAppWidget(componentName, views);
		
		updateNotification(views);
	}


	private void updateNotification(RemoteViews views) {

		// Instantiate a Builder object.
		Notification.Builder builder = new Notification.Builder(this);
		builder.setContentTitle("Picture Download")
	    .setContentText("Download in progress")
	    .setSmallIcon(R.drawable.circle_battery_icon)
	    .setContent(views);
		// Creates an Intent for the Activity
	/*	Intent notifyIntent =
		        new Intent(new ComponentName(this, ResultActivity.class));
	*/	// Sets the Activity to start in a new, empty task
	/*	notifyIntent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK);
		// Creates the PendingIntent
		PendingIntent notifyIntent =
		        PendingIntent.getActivity(
		        this,
		        0,
		        notifyIntent
		        PendingIntent.FLAG_UPDATE_CURRENT
		);
*/
		// Puts the PendingIntent into the notification builder
//		builder.setContentIntent(notifyIntent);
		// Notifications are issued by sending them to the
		// NotificationManager system service.
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// Builds an anonymous Notification object from the builder, and
		// passes it to the NotificationManager
		mNotificationManager.notify(0, builder.build());
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
