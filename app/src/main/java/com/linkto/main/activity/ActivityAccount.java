package com.linkto.main.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.linkto.main.core.Server;
import com.linkto.main.util.Util;
import com.linkto.main.util.Encryption;
import com.linkto.main.core.Eos;
import com.linkto.main.util.Storage;
import com.linkto.main.view.DialogPassword;
import com.linkto.main.view.DialogSimple;
import com.linkto.scatter.R;

import org.json.JSONObject;

public class ActivityAccount extends ActivityBase {
	private static ActivityAccount sInstance;
	public static ActivityAccount getInstance() {
		return sInstance;
	}

	private TextView mTvName;
	private Button mBtnOpen;
	private TextView mTvInfo;

	private String mName;

	private Handler mHandler;
	private Runnable mServerCheck = new Runnable() {
		@Override
		public void run() {
			if (Server.isRunning()) {
				mBtnOpen.setText(mName == null ? R.string.open : R.string.close);
				return;
			}

			mHandler.postDelayed(mServerCheck, 200);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sInstance = this;

		setContentView(R.layout.activity_account);

		mTvName = findViewById(R.id.tv_name);
		mTvName.setOnClickListener((view) -> updateInfo());

		mBtnOpen = findViewById(R.id.btn_open);
		mBtnOpen.setText(R.string.wait_server);
		mBtnOpen.setOnClickListener((view) -> {
			if (!Server.isRunning()) {
				Toast.makeText(this, R.string.wait_server, Toast.LENGTH_SHORT).show();
			} else if (mName == null) {
				open();
			} else {
				close();
			}
		});

		mHandler = new Handler(Looper.getMainLooper());
		mHandler.post(mServerCheck);

		findViewById(R.id.btn_delete).setOnClickListener((view) -> {
			DialogSimple dialogSimple = new DialogSimple(this);
			dialogSimple.setContent(R.string.delete_account_hint);
			dialogSimple.setButton(R.string.delete, R.string.cancel);
			dialogSimple.setOnClickListener((index) -> {
				if (index == 0) {
					Storage.remove(Util.PRIVATE_KEY_CIPHER);
					Toast.makeText(ActivityAccount.this, R.string.delete_account_success,
							Toast.LENGTH_SHORT).show();
					backToMain();
				}
			});
			dialogSimple.show();
		});

		mTvInfo = findViewById(R.id.tv_info);
	}

	private void open() {
		String cipher = Storage.getString(Util.PRIVATE_KEY_CIPHER, null);
		if (cipher == null) {
			Storage.remove(Util.PRIVATE_KEY_CIPHER);
			Toast.makeText(this, R.string.not_valid_private, Toast.LENGTH_SHORT).show();
			backToMain();
			return;
		}

		DialogPassword dialogPassword = new DialogPassword(this);
		dialogPassword.setOnClickListener((password) -> {
			if (password == null) {
				return;
			}

			String privateKey = Encryption.decode(password, cipher);
			String publicKey = Eos.privateToPublic(privateKey);
			if (publicKey == null) {
				Toast.makeText(ActivityAccount.this,
						R.string.password_error,
						Toast.LENGTH_SHORT).show();
				return;
			}

			new Thread(() -> {
				String name = Eos.getKeyAccounts(publicKey);

				runOnUiThread(() -> {
					if (name == null) {
						Toast.makeText(ActivityAccount.this, R.string.account_not_exist,
								Toast.LENGTH_SHORT).show();
						backToMain();
						return;
					}

					mName = name;
					Server.getScatter().setInfo(name, privateKey, publicKey);

					mTvName.setText(name);
					mBtnOpen.setText(R.string.close);

					updateInfo();
				});
			}).start();
		});
		dialogPassword.show();
	}

	private void close() {
		mName = null;
		Server.getScatter().setInfo(null, null, null);

		mTvName.setText(R.string.unopened);
		mBtnOpen.setText(R.string.open);
		mTvInfo.setText("");
	}

	private void backToMain() {
		Intent intent = new Intent(this, ActivityMain.class);
		startActivity(intent);

		finish();
	}

	private void updateInfo() {
		new Thread(() -> {
			JSONObject info = Eos.getAccount(mName);
			runOnUiThread(() -> {
				try {
					String balance = info.optString("core_liquid_balance");

					int ramTotal = info.optInt("ram_quota");
					int ramLeft = ramTotal - info.optInt("ram_usage");
					if (ramLeft < 0) {
						ramLeft = 0;
					}

					JSONObject cpu = info.optJSONObject("cpu_limit");
					int cpuTotal = cpu.optInt("available");
					int cpuLeft = cpuTotal - cpu.optInt("used");
					if (cpuLeft < 0) {
						cpuLeft = 0;
					}

					JSONObject net = info.optJSONObject("net_limit");
					int netTotal = net.optInt("available");
					int netLeft = netTotal - net.optInt("used");
					if (netLeft < 0) {
						netLeft = 0;
					}

					mTvInfo.setText(getString(R.string.account_info,
							balance,
							ramLeft, ramTotal,
							cpuLeft, cpuTotal,
							netLeft, netTotal));
				} catch (Exception e) {
					Log.e(Util.TAG, "updateInfo", e);
				}
			});
		}).start();
	}
}
