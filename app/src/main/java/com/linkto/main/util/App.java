package com.linkto.main.util;

import android.app.Application;

public class App extends Application {
	@Override
	public void onCreate() {
		super.onCreate();

		Util.init(this);
	}
}
