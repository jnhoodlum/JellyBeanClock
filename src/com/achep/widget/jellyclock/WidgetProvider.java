/*
 * Copyright (C) 2012 AChep@xda <artemchep@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.achep.widget.jellyclock;

import com.achep.widget.jellyclock.R;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.IBinder;
import android.text.format.Time;
import android.widget.RemoteViews;

/**
 * Simple widget to show analog clock.
 */
public class WidgetProvider extends AppWidgetProvider {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(
				"android.appwidget.action.APPWIDGET_DISABLED")) {

			// Stop update service
			context.stopService(new Intent(context, UpdateService.class));
		} else {

			// Oh :')
			context.startService(new Intent(context, UpdateService.class));
		}
	}

	public static class UpdateService extends Service {

		private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

			private boolean isScreenOn = true;

			@Override
			public void onReceive(Context context, Intent intent) {
				String key = intent.getAction();
				if (key.equals(Intent.ACTION_SCREEN_ON)) {
					isScreenOn = true;
					updateWidget(0);
				} else if (key.equals(Intent.ACTION_SCREEN_OFF)) {
					isScreenOn = false;
				} else if (isScreenOn) {
					updateWidget(0);
				}
			}

		};

		@Override
		public void onCreate() {
			// Register time change / screen actions
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_SCREEN_ON);
			filter.addAction(Intent.ACTION_SCREEN_OFF);
			filter.addAction(Intent.ACTION_TIME_TICK);
			filter.addAction(Intent.ACTION_TIME_CHANGED);
			filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
			registerReceiver(mReceiver, filter);
		}

		@Override
		public void onDestroy() {

			// We're destroying :'(
			unregisterReceiver(mReceiver);
		}

		@Override
		public void onStart(Intent intent, int startId) {

			// Update all elements and set onclick intent
			updateWidget(1);
		}

		@Override
		public IBinder onBind(Intent arg0) {
			return null;
		}

		private void save(RemoteViews rv) {
			AppWidgetManager.getInstance(this).updateAppWidget(
					new ComponentName(this, WidgetProvider.class), rv);
		}

		private Intent getAlarmIntent() {
			PackageManager packageManager = getPackageManager();
			Intent alarmClockIntent = new Intent(Intent.ACTION_MAIN)
					.addCategory(Intent.CATEGORY_LAUNCHER);
			String clockImpls[][] = {
					{ "HTC Alarm ClockDT", "com.htc.android.worldclock",
							"com.htc.android.worldclock.WorldClockTabControl" },
					{ "Standar Alarm ClockDT", "com.android.deskclock",
							"com.android.deskclock.AlarmClock" },
					{ "Froyo Nexus Alarm ClockDT",
							"com.google.android.deskclock",
							"com.android.deskclock.DeskClock" },
					{ "Moto Blur Alarm ClockDT",
							"com.motorola.blur.alarmclock",
							"com.motorola.blur.alarmclock.AlarmClock" },
					{ "Samsung Galaxy S", "com.sec.android.app.clockpackage",
							"com.sec.android.app.clockpackage.ClockPackage" } };
			boolean foundClockImpl = false;
			for (int i = 0; i < clockImpls.length; i++) {
				String packageName = clockImpls[i][1];
				String className = clockImpls[i][2];
				try {
					ComponentName cn = new ComponentName(packageName, className);
					packageManager.getActivityInfo(cn,
							PackageManager.GET_META_DATA);
					alarmClockIntent.setComponent(cn);
					foundClockImpl = true;
				} catch (NameNotFoundException nf) {
				}
			}
			return (foundClockImpl) ? alarmClockIntent : null;
		}

		private void updateWidget(int mode) {
			RemoteViews rv = new RemoteViews(getPackageName(),
					R.layout.analog_appwidget);
			Resources r = getResources();

			// // ON CLICK ON CLICK ON CLICK
			// // ON CLICK ON CLICK ON CLICK
			// // ON CLICK ON CLICK ON CLICK

			if (mode == 1) {
				rv.setOnClickPendingIntent(R.id.analog_appwidget,
						PendingIntent.getActivity(this, 0, getAlarmIntent(), 0));
			}

			// // PREPARING PREPARING PREPARING
			// // PREPARING PREPARING PREPARING
			// // PREPARING PREPARING PREPARING

			// Create new bitmap and canvas
			float bitmapSize = r.getDimension(R.dimen.widget_bitmapsize);
			Bitmap bitmap = Bitmap.createBitmap(Math.round(bitmapSize),
					Math.round(bitmapSize), Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);

			// X/Y of center
			float center = bitmapSize / 2;

			// Init paint with our settings
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setStyle(Paint.Style.STROKE);
			paint.setColor(r.getColor(R.color.widget_stroke_color));
			paint.setShadowLayer(r.getDimension(R.dimen.widget_shadow_radius),
					0, 0, r.getColor(R.color.widget_shadow_color));
			paint.setStrokeWidth(r.getDimension(R.dimen.widget_stroke_width));

			// Get current time
			Time time = new Time();
			time.setToNow();

			// // DRAWING DRAWING DRAWING
			// // DRAWING DRAWING DRAWING
			// // DRAWING DRAWING DRAWING

			// Draw clock circle
			canvas.drawCircle(center, center,
					r.getDimension(R.dimen.widget_circle_radius), paint);

			// Draw hour hand
			canvas.save();
			canvas.rotate(time.hour * 30 + time.minute / 2, center, center);
			canvas.drawLine(center,
					center - r.getDimension(R.dimen.widget_hand_hour), center,
					center + r.getDimension(R.dimen.widget_hand_hour_over),
					paint);
			canvas.restore();

			// Draw minute hand
			canvas.save();
			canvas.rotate(time.minute * 6, center, center);
			canvas.drawLine(center,
					center - r.getDimension(R.dimen.widget_hand_minute),
					center,
					center + r.getDimension(R.dimen.widget_hand_minute_over),
					paint);
			canvas.restore();

			// // SET CHANGES SET CHANGES
			// // SET CHANGES SET CHANGES
			// // SET CHANGES SET CHANGES

			rv.setImageViewBitmap(R.id.analog_appwidget, bitmap);
			save(rv);

			// CLEAN UP !!!
			bitmap.recycle();
		}
	}
}