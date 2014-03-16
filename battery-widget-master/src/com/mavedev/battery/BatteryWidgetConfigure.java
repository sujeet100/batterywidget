package com.mavedev.battery;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class BatteryWidgetConfigure extends Activity {
	
	private static final int DEFAULT_WARNING_LEVEL = 30;
	private static final int DEFAULT_CRITICAL_LEVEL = 15;
	
	int widgetId;
	SeekBar warnSeekBar;
	SeekBar critSeekBar;
	TextView warnLab;
	TextView critLab;
	Button doneButton;
	
	public static int criticalLevel;
	public static int warningLevel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.config_layout);
		warnSeekBar = (SeekBar) findViewById(R.id.warningLevel);
		critSeekBar = (SeekBar) findViewById(R.id.criticalLevel);
		warnLab = (TextView) findViewById(R.id.warnPerc);
		critLab = (TextView) findViewById(R.id.critPerc);
		doneButton = (Button) findViewById(R.id.doneButton);
		
		warnSeekBar.setProgress(DEFAULT_WARNING_LEVEL);
		critSeekBar.setProgress(DEFAULT_CRITICAL_LEVEL);
		warnLab.setText(DEFAULT_WARNING_LEVEL+"%");
		critLab.setText(DEFAULT_CRITICAL_LEVEL+"%");
		
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
		    widgetId = extras.getInt(
		            AppWidgetManager.EXTRA_APPWIDGET_ID, 
		            AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		
		warnSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {

				warnLab.setText(progress+"%");
				warningLevel = progress;
				if(warningLevel <= criticalLevel){
					critSeekBar.setProgress(warningLevel<=10? 5:warningLevel - 10);
				}
				
			}
		});
		
		critSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {

				critLab.setText(progress+"%");
				criticalLevel = progress;
				if(warningLevel <= criticalLevel){
					warnSeekBar.setProgress(criticalLevel>=90? 95:criticalLevel + 10);
				}
			}
		});

		
		
		doneButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final Context context = BatteryWidgetConfigure.this;

	            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
	            RemoteViews views = new RemoteViews(context.getPackageName(),
	            		R.layout.widget_layout);
	            
	            appWidgetManager.updateAppWidget(widgetId, views);

	            Intent resultValue = new Intent();
	            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
	            setResult(RESULT_OK, resultValue);
	            BatteryWidget.updateView(context, appWidgetManager, widgetId);
	            
	            finish();
				
			}
		});
	}
	
}
