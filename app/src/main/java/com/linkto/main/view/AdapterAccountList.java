package com.linkto.main.view;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.linkto.main.activity.ActivityAccountInfo;
import com.linkto.main.activity.ActivityBase;
import com.linkto.main.core.AccountInfo;
import com.linkto.main.core.AccountMgr;
import com.linkto.main.core.AccountThumb;
import com.linkto.scatter.R;

public class AdapterAccountList extends BaseAdapter {
	private ActivityBase mActivity;

	public AdapterAccountList(ActivityBase activity) {
		mActivity = activity;
	}

	@Override
	public int getCount() {
		return AccountMgr.getAccountCount();
	}

	@Override
	public Object getItem(int index) {
		return null;
	}

	@Override
	public long getItemId(int index) {
		return 0;
	}

	@Override
	public View getView(int index, View view, ViewGroup parent) {
		AccountThumb data = AccountMgr.getAccountThumb(index);

		if (view == null) {
			view = LayoutInflater.from(mActivity).inflate(R.layout.grid_account, null);
		}

		if (data == null) {
			return view;
		}

		TextView tvName = view.findViewById(R.id.tv_name);
		tvName.setText(data.name);

		TextView tvKey = view.findViewById(R.id.tv_key);
		tvKey.setText(data.publicKey);

		Button btnOpen = view.findViewById(R.id.btn_open);
		btnOpen.setOnClickListener((v) -> {
			AccountInfo ai = AccountMgr.getOpenedAccount();
			if (ai == null || !data.publicKey.equals(ai.publicKey)) {
				AccountMgr.openAccount(mActivity, data.cipher);
			} else {
				AccountMgr.closeAccount();
			}
		});

		AccountInfo ai = AccountMgr.getOpenedAccount();
		btnOpen.setText(ai != null && data.publicKey.equals(ai.publicKey) ? R.string.close : R.string.open);

		view.findViewById(R.id.btn_remove).setOnClickListener((v) -> {
			DialogSimple dialogSimple = new DialogSimple(mActivity);
			dialogSimple.setContent(R.string.remove_account_hint);
			dialogSimple.setButton(R.string.remove, R.string.cancel);
			dialogSimple.setOnClickListener((idx) -> {
				if (idx == 0) {
					AccountMgr.removeAccount(data.publicKey);
				}
			});
			dialogSimple.show();
		});

		view.setOnClickListener((v) -> {
			Intent intent = new Intent();
			intent.setClass(mActivity, ActivityAccountInfo.class);
			intent.putExtra(AccountMgr.KEY_NAME, data.name);
			mActivity.startActivity(intent);
		});

		return view;
	}
}
