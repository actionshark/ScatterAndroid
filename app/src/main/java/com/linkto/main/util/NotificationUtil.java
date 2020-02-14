package com.linkto.main.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;

import com.linkto.main.activity.ActivityAccountList;
import com.linkto.scatter.R;

public class NotificationUtil {
	private static final int ID = 1;

	public static void showNotification(Context context, int count) {
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder builder;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			String id = context.getPackageName();
			String name = context.getString(R.string.app_name);

			NotificationChannel channel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW);
			nm.createNotificationChannel(channel);

			builder = new Notification.Builder(context, id);
		} else {
			builder = new Notification.Builder(context);
		}

		Intent intent = new Intent(context, ActivityAccountList.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

		Notification nt = builder
				.setContentTitle(context.getString(R.string.notification_title))
				.setContentText(context.getString(count > 0 ? R.string.notification_request : R.string.notification_empty))
				.setSmallIcon(R.drawable.notification)
				.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon))
				.setContentIntent(pendingIntent)
				.setAutoCancel(false)
				.setOngoing(true)
				.setVisibility(Notification.VISIBILITY_PRIVATE)
				.build();

		if (context instanceof Service) {
			((Service) context).startForeground(ID, nt);
		} else {
			nm.notify(ID, nt);
		}
	}

	public static void cancelNotification(Context context) {
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(ID);
	}
}
