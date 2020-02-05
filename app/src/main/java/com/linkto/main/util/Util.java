package com.linkto.main.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

import com.linkto.main.activity.ActivityAccount;
import com.linkto.main.core.Eos;
import com.linkto.main.core.Server;

import java.util.List;

public class Util {
	public static final String TAG = "ScatterLog";

	public static final int PASSWORD_LENGTH_MIN = 3;
	public static final int PASSWORD_LENGTH_MAX = 20;

	public static final String PRIVATE_KEY_CIPHER = "private_key_cipher";

	private static boolean sInited = false;

	public synchronized static void init(Context context) {
		if (sInited) {
			return;
		}

		sInited = true;

		Storage.init(context);
		Server.init();
	}

	private static final char[] HEX_CHARS = new char[]{
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'a', 'b', 'c', 'd', 'e', 'f',
	};

	public static String bytesToHex(byte[] bs) {
		StringBuilder sb = new StringBuilder();

		for (byte b : bs) {
			sb.append(HEX_CHARS[(b >> 4) & 0xf]);
			sb.append(HEX_CHARS[b & 0xf]);
		}

		return sb.toString();
	}

	public static int charToInt(char ch) {
		if (ch >= '0' && ch <= '9') {
			return ch - '0';
		}

		if (ch >= 'a' && ch <= 'z') {
			return ch - 'a' + 10;
		}

		if (ch >= 'A' && ch <= 'Z') {
			return ch - 'A' + 10;
		}

		return 0;
	}

	public static byte[] hexToBytes(String hex) {
		byte[] bs = new byte[hex.length() / 2];

		for (int i = 0; i < bs.length; i++) {
			int a = charToInt(hex.charAt(i << 2));
			int b = charToInt(hex.charAt((i << 2) + 1));
			bs[i] = (byte) ((a << 4) | b);
		}

		return bs;
	}

	public static void changeToForeground(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> taskList = am.getRunningTasks(100);
		for (ActivityManager.RunningTaskInfo rti : taskList) {
			if (rti.topActivity.getPackageName().equals(context.getPackageName())) {
				am.moveTaskToFront(rti.id, 0);
				break;
			}
		}
	}

	public static void changeToBackground(Activity activity) {
		activity.moveTaskToBack(true);
	}
}
