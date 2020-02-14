package com.linkto.main.core;

import android.util.Log;

import com.linkto.main.activity.ActivityBase;
import com.linkto.main.activity.ForegroundService;
import com.linkto.main.util.App;
import com.linkto.main.util.Broadcast;
import com.linkto.main.util.Encryption;
import com.linkto.main.util.Storage;
import com.linkto.main.util.Util;
import com.linkto.main.view.DialogPassword;
import com.linkto.scatter.R;

import org.json.JSONArray;
import org.json.JSONObject;

public class AccountMgr {
	public static final String KEY_ACCOUNTS = "accounts";
	public static final String KEY_NAME = "name";
	public static final String KEY_CIPHER = "cipher";
	public static final String KEY_PUBLIC_KEY = "public_key";

	private static JSONArray sAccounts;

	private static AccountInfo sOpenedAccount;

	public static void init() {
		try {
			sAccounts = new JSONArray();

			String str = Storage.getString(KEY_ACCOUNTS, null);
			sAccounts = new JSONArray(str);
		} catch (Exception e) {
			Log.e(Util.TAG, "AccountMgr", e);
		}

		Storage.set(KEY_ACCOUNTS, sAccounts);
	}

	public static synchronized AccountInfo getOpenedAccount() {
		return sOpenedAccount;
	}

	public static int getAccountCount() {
		return sAccounts.length();
	}

	public static AccountThumb getAccountThumb(int index) {
		AccountThumb account = new AccountThumb();

		synchronized (sAccounts) {
			JSONObject jo = sAccounts.optJSONObject(index);
			if (jo == null) {
				return null;
			}

			account.name = jo.optString(KEY_NAME);
			account.cipher = jo.optString(KEY_CIPHER);
			account.publicKey = jo.optString(KEY_PUBLIC_KEY);
		}

		return account;
	}

	public static void importAccount(String privateKey, String password) {
		new Thread(() -> {
			try {
				if (password == null || password.length() < Util.PASSWORD_LENGTH_MIN
						|| password.length() > Util.PASSWORD_LENGTH_MAX) {

					Util.showToast(R.string.password_length_error,
							Util.PASSWORD_LENGTH_MIN, Util.PASSWORD_LENGTH_MAX);
					return;
				}

				String publicKey = Eos.privateToPublic(privateKey);
				if (publicKey == null) {
					Util.showToast(R.string.not_valid_private);
					return;
				}

				synchronized (sAccounts) {
					for (int i = 0; i < sAccounts.length(); i++) {
						String key = sAccounts.optJSONObject(i).optString(KEY_PUBLIC_KEY);
						if (publicKey.equals(key)) {
							Util.showToast(R.string.account_has_imported);
							return;
						}
					}
				}

				String name = Eos.getKeyAccounts(publicKey);
				if (name == null) {
					Util.showToast(R.string.account_not_exist);
					return;
				}

				String cipher = Encryption.encode(password, privateKey);

				JSONObject account = new JSONObject();
				account.put(KEY_NAME, name);
				account.put(KEY_CIPHER, cipher);
				account.put(KEY_PUBLIC_KEY, publicKey);

				synchronized (sAccounts) {
					for (int i = 0; i < sAccounts.length(); i++) {
						String key = sAccounts.optJSONObject(i).optString(KEY_PUBLIC_KEY);
						if (publicKey.equals(key)) {
							Util.showToast(R.string.account_has_imported);
							return;
						}
					}

					sAccounts.put(account);
				}

				Storage.set(KEY_ACCOUNTS, sAccounts);

				Broadcast.sendMessage(Broadcast.NAME_ACCOUNT_IMPORTED, name);
				Util.showToast(R.string.import_account_success);
			} catch (Exception e) {
				Log.e(Util.TAG, "import account", e);
				Util.showToast(R.string.import_account_failed);
			}
		}).start();
	}

	public static void removeAccount(String publicKey) {
		try {
			synchronized (sAccounts) {
				for (int i = 0; i < sAccounts.length(); i++) {
					String key = sAccounts.optJSONObject(i).optString(KEY_PUBLIC_KEY);
					if (publicKey.equals(key)) {
						sAccounts.remove(i);
						Storage.set(KEY_ACCOUNTS, sAccounts);

						Broadcast.sendMessage(Broadcast.NAME_ACCOUNT_REMOVED, publicKey);
						Util.showToast(R.string.remove_account_success);
						return;
					}
				}
			}

			Util.showToast(R.string.account_not_exist);
		} catch (Exception e) {
			Log.e(Util.TAG, "remove account", e);
			Util.showToast(R.string.remove_account_failed);
		}
	}

	public static void openAccount(ActivityBase activity, String cipher) {
		DialogPassword dialogPassword = new DialogPassword(activity);
		dialogPassword.setOnClickListener((password) -> {
			if (password == null) {
				return;
			}

			String privateKey = Encryption.decode(password, cipher);
			String publicKey = Eos.privateToPublic(privateKey);
			if (publicKey == null) {
				Util.showToast(R.string.password_error);
				return;
			}

			AccountInfo ai = new AccountInfo();
			ai.privateKey = privateKey;
			ai.publicKey = publicKey;

			new Thread(() -> {
				ai.name = Eos.getKeyAccounts(publicKey);
				if (ai.name == null) {
					Util.showToast(R.string.account_not_exist);
					return;
				}

				synchronized (AccountMgr.class) {
					sOpenedAccount = ai;
				}

				ForegroundService.showService(App.getInstance(), 0);

				Util.showToast(R.string.account_open_success);
				Broadcast.sendMessage(Broadcast.NAME_ACCOUNT_OPENED, ai);

				updateAccountInfo(ai);
			}).start();
		});
		dialogPassword.show();
	}

	public static void closeAccount() {
		AccountInfo old;

		synchronized (AccountMgr.class) {
			old = sOpenedAccount;
			sOpenedAccount = null;
		}

		Broadcast.sendMessage(Broadcast.NAME_ACCOUNT_CLOSED, old);
	}

	public static void updateAccountInfo(AccountInfo ai) {
		new Thread(() -> {
			try {
				JSONObject info = Eos.getAccount(ai.name);

				ai.balance = info.optString("core_liquid_balance");

				ai.ram.total = info.optInt("ram_quota");
				ai.ram.left = ai.ram.total - info.optInt("ram_usage");

				JSONObject cpu = info.optJSONObject("cpu_limit");
				ai.cpu.total = cpu.optInt("max");
				ai.cpu.left = ai.cpu.total - cpu.optInt("used");

				JSONObject net = info.optJSONObject("net_limit");
				ai.net.total = net.optInt("max");
				ai.net.left = ai.net.total - net.optInt("used");

				Broadcast.sendMessage(Broadcast.NAME_ACCOUNT_UPDATED, ai);
			} catch (Exception e) {
				Log.e(Util.TAG, "updateAccountInfo", e);
			}
		}).start();
	}
}
