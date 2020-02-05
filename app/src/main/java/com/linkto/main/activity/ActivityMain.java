package com.linkto.main.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.linkto.main.core.Server;
import com.linkto.main.util.Util;
import com.linkto.main.core.Eos;
import com.linkto.main.util.Storage;
import com.linkto.scatter.R;

public class ActivityMain extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		Util.init(this);

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