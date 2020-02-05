package com.linkto.main.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.linkto.main.util.Util;
import com.linkto.main.util.Storage;
import com.linkto.scatter.R;

import io.eblock.eos4j.Ecc;

public class ActivityMain extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		boolean hasKey = Storage.contains(Util.PRIVATE_KEY_CIPHER);
		Intent intent = new Intent();

		if (hasKey) {
			intent.setClass(this, ActivityAccount.class);
		} else {
			intent.setClass(this, ActivityEditKey.class);
		}

		startActivity(intent);
		finish();
	}
}
