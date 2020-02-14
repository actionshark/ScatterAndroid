package com.linkto.main.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ListView;

import com.linkto.main.util.Broadcast;
import com.linkto.main.util.Util;
import com.linkto.main.view.AdapterAccountList;
import com.linkto.scatter.R;

public class ActivityAccountList extends ActivityBase implements Broadcast.Callback {
	private AdapterAccountList mAdapterAccountList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_account_list);

		ListView lvAccountList = findViewById(R.id.lv_list);
		mAdapterAccountList = new AdapterAccountList(this);
		lvAccountList.setAdapter(mAdapterAccountList);

		findViewById(R.id.btn_import).setOnClickListener((view) -> {
			Intent intent = new Intent();
			intent.setClass(this, ActivityImportAccount.class);
			startActivity(intent);
		});

		Broadcast.addListener(Broadcast.NAME_ACCOUNT_IMPORTED, this, true);
		Broadcast.addListener(Broadcast.NAME_ACCOUNT_REMOVED, this, true);
		Broadcast.addListener(Broadcast.NAME_ACCOUNT_OPENED, this, true);
		Broadcast.addListener(Broadcast.NAME_ACCOUNT_CLOSED, this, true);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		Broadcast.removeListener(null, this);
	}

	@Override
	public void onBroadcast(String name, Object data) {
		mAdapterAccountList.notifyDataSetChanged();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Util.changeToBackground(this);
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
}
