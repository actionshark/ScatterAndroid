package com.linkto.main.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Broadcast {
	public static final String NAME_ACCOUNT_IMPORTED = "account_imported";
	public static final String NAME_ACCOUNT_REMOVED = "account_removed";

	public static final String NAME_ACCOUNT_OPENED = "account_opened";
	public static final String NAME_ACCOUNT_CLOSED = "account_closed";

	public static final String NAME_ACCOUNT_UPDATED = "account_updated";

	public interface Callback {
		void onBroadcast(String name, Object data) throws Exception;
	}

	private static class Listener {
		String name;
		Callback callback;
		boolean onMainThread;
	}

	private static final List<Listener> sListeners = new ArrayList<>();

	public static synchronized void addListener(String name, Callback callback, boolean onMainThread) {
		if (name == null || callback == null) {
			return;
		}

		Listener listener = new Listener();
		listener.name = name;
		listener.callback = callback;
		listener.onMainThread = onMainThread;

		sListeners.add(listener);
	}

	public static synchronized void removeListener(String name, Callback callback) {
		for (int i = sListeners.size() - 1; i >= 0; i--) {
			Listener listener = sListeners.get(i);

			if ((name == null || name.equals(listener.name)) && callback == listener.callback) {
				sListeners.remove(i);
			}
		}
	}

	public static synchronized void sendMessage(String name, Object data) {
		for (Listener listener : sListeners) {
			if (!listener.name.equals(name)) {
				continue;
			}

			Runnable runnable = () -> {
				try {
					listener.callback.onBroadcast(name, data);
				} catch (Exception e) {
					Log.e(Util.TAG, "broadcast", e);
				}
			};

			if (listener.onMainThread) {
				Util.runOnMainThread(runnable);
			} else {
				new Thread(runnable).start();
			}
		}
	}
}
