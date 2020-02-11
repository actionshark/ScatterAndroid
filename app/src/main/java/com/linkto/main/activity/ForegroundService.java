package com.linkto.main.activity;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.linkto.main.util.NotificationUtil;

public class ForegroundService extends Service {
	private static ForegroundService sInstance;

	private static final String KEY_COUNT = "count";

	public static void showService(Context context, int count) {
		if (sInstance == null) {
			Intent intent = new Intent(context, ForegroundService.class);
			intent.putExtra(KEY_COUNT, count);
			context.startService(intent);
		} else {
			sInstance.showNotification(count);
		}
	}

	public static void cancelSerice(Context context) {
		Intent intent = new Intent(context, ForegroundService.class);
		context.stopService(intent);

		NotificationUtil.cancelNotification(context);
	}

	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		sInstance = this;
	}

	@Override
	public int onStartCommand(Intent intent, int flag, int startId) {
		int count = intent.getIntExtra(KEY_COUNT, 0);
		showNotification(count);

		return super.onStartCommand(intent, flag, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		sInstance = null;
	}

	private void showNotification(int count) {
		NotificationUtil.showNotification(this, count);
	}
}
