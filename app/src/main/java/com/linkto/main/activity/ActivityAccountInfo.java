package com.linkto.main.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.linkto.main.core.AccountInfo;
import com.linkto.main.core.AccountMgr;
import com.linkto.main.util.Broadcast;
import com.linkto.scatter.R;

public class ActivityAccountInfo extends ActivityBase implements Broadcast.Callback {
	private TextView mTvName;
	private TextView mTvInfo;

	private String mName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		mName = intent.getStringExtra(AccountMgr.KEY_NAME);

		setContentView(R.layout.activity_account_info);

		mTvName = findViewById(R.id.tv_name);
		mTvName.setText(mName);
		mTvName.setOnClickListener((view) -> requestUpdateInfo());

		mTvInfo = findViewById(R.id.tv_info);

		Broadcast.addListener(Broadcast.NAME_ACCOUNT_UPDATED, this, true);
		requestUpdateInfo();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		Broadcast.removeListener(null, this);
	}

	private void requestUpdateInfo() {
		AccountInfo ai = new AccountInfo();
		ai.name = mName;
		AccountMgr.updateAccountInfo(ai);
	}

	@Override
	public void onBroadcast(String name, Object data) {
		if (Broadcast.NAME_ACCOUNT_UPDATED.equals(name)) {
			AccountInfo ai = (AccountInfo) data;

			if (mName.equals(ai.name)) {
				onUpdateInfo(ai);
			}
		}
	}

	private void onUpdateInfo(AccountInfo ai) {
		mTvInfo.setText(getString(R.string.account_info,
				ai.balance,
				ai.ram.left, ai.ram.total,
				ai.cpu.left, ai.cpu.total,
				ai.net.left, ai.net.total));
	}
}
