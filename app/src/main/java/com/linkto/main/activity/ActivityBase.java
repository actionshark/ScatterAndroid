package com.linkto.main.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.linkto.main.util.App;
import com.linkto.main.util.Util;

import java.util.ArrayList;
import java.util.List;

public abstract class ActivityBase extends Activity {
	public interface Task {
		void onTask(Activity activity) throws Exception;
	}

	private static final List<ActivityBase> sActivities = new ArrayList<>();

	private static final List<Task> sTasks = new ArrayList<>();

	public static void post(Task task) {
		ActivityBase activity;

		synchronized (sActivities) {
			if (sActivities.size() > 0) {
				activity = sActivities.get(sActivities.size() - 1);
			} else {
				activity = null;
			}
		}

		if (activity != null) {
			activity.runOnUiThread(() -> {
				try {
					task.onTask(activity);
				} catch (Exception e) {
					Log.e(Util.TAG, "task", e);
				}
			});

			return;
		}

		synchronized (sTasks) {
			sTasks.add(task);
		}

		Context context = App.getInstance();

		Intent intent = new Intent();
		intent.setClass(context, ActivityAccount.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		synchronized (sActivities) {
			sActivities.add(this);
		}

		checkTask();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		synchronized (sActivities) {
			for (int i = sActivities.size() - 1; i >= 0; i--) {
				if (sActivities.get(i) == this) {
					sActivities.remove(i);
				}
			}
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		checkTask();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	private void checkTask() {
		while (true) {
			Task task = null;
			synchronized (sTasks) {
				if (sTasks.size() > 0) {
					task = sTasks.remove(0);
				}
			}

			if (task == null) {
				break;
			}

			try {
				task.onTask(this);
			} catch (Exception e) {
				Log.e(Util.TAG, "task", e);
			}
		}
	}
}
