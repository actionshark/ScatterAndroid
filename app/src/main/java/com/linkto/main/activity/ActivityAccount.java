package com.linkto.main.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.linkto.main.core.Server;
import com.linkto.main.util.Util;
import com.linkto.main.util.Encryption;
import com.linkto.main.core.Eos;
import com.linkto.main.util.Storage;
import com.linkto.main.view.DialogPassword;
import com.linkto.scatter.R;

import org.json.JSONObject;

public class ActivityAccount extends Activity {
	private TextView mTvName;
	private Button mBtnOpen;
	private TextView mTvInfo;

	private String mName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_account);

		mTvName = findViewById(R.id.tv_name);
		mTvName.setOnClickListener((view) -> updateInfo());

		mBtnOpen = findViewById(R.id.btn_open);
		mBtnOpen.setOnClickListener((view) -> {
			if (mName == null) {
				open();
			} else {
				close();
			}
		});

		findViewById(R.id.btn_delete).setOnClickListener((view) -> {
			Storage.remove(Util.PRIVATE_KEY_CIPHER);
			Toast.makeText(ActivityAccount.this, R.string.delete_account_success,
					Toast.LENGTH_SHORT).show();
			backToMain();
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

					JSONObject cpu = info.optJSONObject("cpu_limit");
					int cpuAvailable = cpu.optInt("available");
					int cpuUsed = cpu.optInt("used");
					if (cpuUsed > cpuAvailable) {
						cpuUsed = cpuAvailable;
					}

					JSONObject net = info.optJSONObject("net_limit");
					int netAvailable = net.optInt("available");
					int netUsed = net.optInt("used");
					if (netUsed > netAvailable) {
						netUsed = netAvailable;
					}

					mTvInfo.setText(getString(R.string.account_info,
							balance, cpuUsed, cpuAvailable, netUsed, netAvailable));
				} catch (Exception e) {
					Log.e(Util.TAG, "updateInfo", e);
				}
			});
		}).start();
	}
}
