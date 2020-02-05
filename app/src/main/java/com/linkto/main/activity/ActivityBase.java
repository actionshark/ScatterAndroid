package com.linkto.main.activity;

import android.app.Activity;
import android.view.KeyEvent;

public abstract class ActivityBase extends Activity {
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
}
