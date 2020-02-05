package com.linkto.main.util;

import android.content.Context;
import android.content.SharedPreferences;

public class Storage {
	private static SharedPreferences sPreferences;

	public static void init(Context context) {
		sPreferences = context.getSharedPreferences("scatter", Context.MODE_PRIVATE);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	public static boolean contains(String key) {
		return sPreferences.contains(key);
	}

	public static void set(String key, Object value) {
		SharedPreferences.Editor editor = sPreferences.edit();

		if (value instanceof String) {
			editor.putString(key, (String) value);
		} else if (value instanceof Integer) {
			editor.putInt(key, (Integer) value);
		} else if (value instanceof Float) {
			editor.putFloat(key, (Float) value);
		} else if (value instanceof Boolean) {
			editor.putBoolean(key, (Boolean) value);
		} else {
			editor.putString(key, String.valueOf(value));
		}

		editor.apply();
	}

	public static void remove(String key) {
		SharedPreferences.Editor editor = sPreferences.edit();
		editor.remove(key);
		editor.apply();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	public static String getString(String key) {
		return getString(key, null);
	}

	public static String getString(String key, String def) {
		return sPreferences.getString(key, def);
	}

	public static int getInt(String key) {
		return getInt(key, 0);
	}

	public static int getInt(String key, int def) {
		return sPreferences.getInt(key, def);
	}

	public static float getFloat(String key) {
		return getFloat(key, 0f);
	}

	public static float getFloat(String key, float def) {
		return sPreferences.getFloat(key, def);
	}

	public static boolean getBoolean(String key) {
		return getBoolean(key, false);
	}

	public static boolean getBoolean(String key, boolean def) {
		return sPreferences.getBoolean(key, def);
	}
}
