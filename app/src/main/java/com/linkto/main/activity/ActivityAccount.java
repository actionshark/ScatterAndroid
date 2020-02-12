package com.linkto.main.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.linkto.main.core.AccountInfo;
import com.linkto.main.core.Eos;
import com.linkto.main.core.Server;
import com.linkto.main.util.App;
import com.linkto.main.util.Encryption;
import com.linkto.main.util.Storage;
import com.linkto.main.util.Util;
import com.linkto.main.view.DialogPassword;
import com.linkto.main.view.DialogSimple;
import com.linkto.scatter.R;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.Queue;

public class ActivityAccount extends ActivityBase {
	public interface Task {
		void onTask(Activity activity) throws Exception;
	}

	private static final Queue<Task> sTasks = new LinkedList<>();

	public static void post(Task task) {
		synchronized (sTasks) {
			sTasks.offer(task);
		}

		Context context = App.getInstance();

		Intent intent = new Intent();
		intent.setClass(context, ActivityAccount.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	private TextView mTvName;
	private Button mBtnOpen;
	private TextView mTvInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_account);

		AccountInfo ai = Server.getScatter().getAccountInfo();

		mTvName = findViewById(R.id.tv_name);
		mTvName.setOnClickListener((view) -> updateInfo());

		mBtnOpen = findViewById(R.id.btn_open);
		mBtnOpen.setText(ai.enabled ? R.string.close : R.string.open);
		mBtnOpen.setOnClickListener((view) -> {
			if (ai.enabled) {
				close();
			} else {
				open();
			}
		});

		findViewById(R.id.btn_delete).setOnClickListener((view) -> {
			DialogSimple dialogSimple = new DialogSimple(this);
			dialogSimple.setContent(R.string.delete_account_hint);
			dialogSimple.setButton(R.string.delete, R.string.cancel);
			dialogSimple.setOnClickListener((index) -> {
				if (index == 0) {
					Storage.remove(Util.PRIVATE_KEY_CIPHER);
					ai.enabled = false;

					Toast.makeText(ActivityAccount.this, R.string.delete_account_success,
							Toast.LENGTH_SHORT).show();
					backToMain();
				}
			});
			dialogSimple.show();
		});

		mTvInfo = findViewById(R.id.tv_info);

		if (ai.enabled) {
			mTvName.setText(ai.name);
			updateInfo();
		}

		onNewIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		while (true) {
			Task task;
			synchronized (sTasks) {
				task = sTasks.poll();
			}

			if (task == null) {
				break;
			}

			try {
				task.onTask(this);
			} catch (Exception e) {
				Log.e(Util.TAG, "onTask", e);
			}
		}
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
				AccountInfo ai = Server.getScatter().getAccountInfo();
				ai.privateKey = privateKey;

				if (!publicKey.equals(ai.publicKey) || ai.name == null) {
					ai.publicKey = publicKey;
					ai.name = Eos.getKeyAccounts(publicKey);
				}

				runOnUiThread(() -> {
					if (ai.name == null) {
						Toast.makeText(ActivityAccount.this, R.string.account_not_exist,
								Toast.LENGTH_SHORT).show();
						backToMain();
						return;
					}

					ai.enabled = true;

					mTvName.setText(ai.name);
					mBtnOpen.setText(R.string.close);

					updateInfo();

					ForegroundService.showService(ActivityAccount.this, 0);
				});
			}).start();
		});
		dialogPassword.show();
	}

	private void close() {
		AccountInfo ai = Server.getScatter().getAccountInfo();
		ai.enabled = false;

		mTvName.setText(R.string.unopened);
		mBtnOpen.setText(R.string.open);
		mTvInfo.setText("");

		ForegroundService.cancelService(this);
	}

	private void backToMain() {
		Intent intent = new Intent(this, ActivityMain.class);
		startActivity(intent);

		finish();
	}

	private void updateInfo() {
		new Thread(() -> {
			AccountInfo ai = Server.getScatter().getAccountInfo();
			JSONObject info = Eos.getAccount(ai.name);
			runOnUiThread(() -> {
				try {
					ai.balance = info.optString("core_liquid_balance");

					ai.ram.total = info.optInt("ram_quota");
					ai.ram.left = ai.ram.total - info.optInt("ram_usage");
					if (ai.ram.left < 0) {
						ai.ram.left = 0;
					}

					JSONObject cpu = info.optJSONObject("cpu_limit");
					ai.cpu.total = cpu.optInt("available");
					ai.cpu.left = ai.cpu.total - cpu.optInt("used");
					if (ai.cpu.left < 0) {
						ai.cpu.left = 0;
					}

					JSONObject net = info.optJSONObject("net_limit");
					ai.net.total = net.optInt("available");
					ai.net.left = ai.net.total - net.optInt("used");
					if (ai.net.left < 0) {
						ai.net.left = 0;
					}

					mTvInfo.setText(getString(R.string.account_info,
							ai.balance,
							ai.ram.left, ai.ram.total,
							ai.cpu.left, ai.cpu.total,
							ai.net.left, ai.net.total));
				} catch (Exception e) {
					Log.e(Util.TAG, "updateInfo", e);
				}
			});
		}).start();
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
