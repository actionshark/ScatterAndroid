package com.linkto.main.core;

import android.util.Log;
import android.view.Gravity;

import com.linkto.main.activity.ActivityAccount;
import com.linkto.main.activity.ActivityBase;
import com.linkto.main.activity.ForegroundService;
import com.linkto.main.util.Hash;
import com.linkto.main.util.Util;
import com.linkto.main.view.DialogSimple;
import com.linkto.scatter.R;

import org.java_websocket.WebSocket;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Scatter {
	private static final String PREFIX_CONNECT = "40/scatter";
	private static final String PREFIX_EVENT = "42/scatter";

	private static final String TYPE_PAIR = "pair";
	private static final String TYPE_PAIRED = "paired";
	private static final String TYPE_REKEY = "rekey";
	private static final String TYPE_API = "api";
	private static final String TYPE_EVENT = "event";

	private interface Callback {
		void onCallback(Object result) throws Exception;
	}


	private static int sRequestCount = 0;

	private static synchronized int modifyRequestCount(int delta) {
		sRequestCount += delta;
		return sRequestCount;
	}

	private static synchronized int getRequestCount() {
		return sRequestCount;
	}

	private final AccountInfo mAccountInfo = new AccountInfo();

	public AccountInfo getAccountInfo() {
		return mAccountInfo;
	}

	public void onMessage(WebSocket webSocket, String message) throws Exception {
		if (!mAccountInfo.enabled) {
			return;
		}

		if (message.startsWith(PREFIX_CONNECT)) {
			Log.d(Util.TAG, "scatter connect");
		} else if (message.startsWith(PREFIX_EVENT)) {
			JSONArray msg = new JSONArray(message.substring(PREFIX_EVENT.length() + 1));
			String type = msg.optString(0);
			JSONObject data = msg.optJSONObject(1).optJSONObject("data");

			String name = "type" + type.substring(0, 1).toUpperCase() + type.substring(1);
			Method method = Scatter.class.getDeclaredMethod(name, WebSocket.class, JSONObject.class);
			method.invoke(this, webSocket, data);
		} else {
			Log.d(Util.TAG, "scatter unknown message: " + message);
		}
	}

	private void send(WebSocket webSocket, String type, JSONObject data) {
		if (data == null) {
			data = new JSONObject();
		}

		JSONArray params = new JSONArray();
		params.put(type);
		params.put(data);

		String message = PREFIX_EVENT + "," + params;
		webSocket.send(message);

		Log.d(Util.TAG, "send: " + message);
	}

	private void showConfirmDialog(Object hint, int gravity, Callback callback) {
		if (!mAccountInfo.enabled) {
			return;
		}

		ActivityBase.post((activity) -> {
			DialogSimple dialog = new DialogSimple(activity);
			dialog.setContent(hint);
			dialog.setContentGravity(gravity);
			dialog.setButton(R.string.cancel, R.string.confirm);
			dialog.setOnClickListener((index) -> {
				try {
					if (callback != null) {
						callback.onCallback(index == 1);
					}
				} catch (Exception e) {
					Log.e(Util.TAG, "confirm", e);
				} finally {
					Util.changeToBackground(activity);
				}
			});

			dialog.setOnShowListener((dlg) -> {
				int count = modifyRequestCount(1);
				ForegroundService.showService(activity, count);
			});

			dialog.setOnDismissListener((dlg) -> {
				int count = modifyRequestCount(-1);
				ForegroundService.showService(activity, count);
			});

			dialog.show();
			Util.changeToForeground(activity);
		});
	}

	private void typePair(WebSocket webSocket, JSONObject data) {
		send(webSocket, TYPE_PAIRED, null);
	}

	private void typeApi(WebSocket webSocket, JSONObject data) throws Exception {
		Callback callback = (result) -> {
			if (result == null) {
				result = new JSONObject();
			}

			JSONObject response = new JSONObject();
			response.put("id", data.opt("id"));
			response.put("result", result);

			send(webSocket, TYPE_API, response);
		};

		String type = data.optString("type");
		String name = "api" + type.substring(0, 1).toUpperCase() + type.substring(1);

		try {
			Method method = Scatter.class.getDeclaredMethod(name, JSONObject.class, Callback.class);
			method.invoke(this, data, callback);
		} catch (Exception e) {
			Log.e(Util.TAG, "Scatter.typeApi", e);

			callback.onCallback(null);
		}
	}

	private void apiIdentityFromPermissions(JSONObject data, Callback callback) throws Exception {
		callback.onCallback(true);
	}

	private void apiGetOrRequestIdentity(JSONObject data, Callback callback) {
		showConfirmDialog(R.string.login_hint, Gravity.CENTER, (confirm) -> {
			if (!(boolean) confirm) {
				callback.onCallback(false);
				return;
			}

			AccountInfo ai = Server.getScatter().getAccountInfo();

			String blockchain = null;

			try {
				blockchain = data.optJSONObject("payload").optJSONObject("fields")
						.optJSONArray("accounts").optJSONObject(0)
						.optString("blockchain");
			} catch (Exception e) {
				Log.e(Util.TAG, "Scatter.apiGetOrRequestIdentity", e);
			}

			if (blockchain == null) {
				blockchain = "eos";
			}

			JSONObject account = new JSONObject();
			account.put("name", ai.name);
			account.put("authority", "active");
			account.put("blockchain", blockchain);
			account.put("publicKey", ai.publicKey);

			JSONArray accounts = new JSONArray();
			accounts.put(account);


			JSONObject id = new JSONObject();
			id.put("name", ai.name);
			id.put("publicKey", ai.publicKey);
			id.put("accounts", accounts);

			callback.onCallback(id);
		});
	}

	private void requestSignature(Object data, List<Action> actions, Callback callback) {
		Object hint = R.string.sign_hint;
		int gravity = Gravity.CENTER;

		if (actions != null && actions.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (Action action : actions) {
				sb.append("account: ").append(action.account).append('\n')
						.append("name: ").append(action.name).append('\n')
						.append("data: ").append(action.json != null ? action.json : action.bin)
						.append("\n\n");
			}

			hint = sb.toString();
			gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
		}

		showConfirmDialog(hint, gravity, (confirm) -> {
			if (!(boolean) confirm) {
				callback.onCallback(false);
				return;
			}

			byte[] bs;

			if (data instanceof String) {
				bs = ((String) data).getBytes();
			} else if (data instanceof JSONArray) {
				JSONArray ja = (JSONArray) data;
				bs = new byte[ja.length()];
				for (int i = 0; i < bs.length; i++) {
					bs[i] = (byte) ja.optInt(i);
				}
			} else if (data instanceof byte[]) {
				bs = (byte[]) data;
			} else {
				throw new IllegalArgumentException("illegal argument");
			}

			AccountInfo ai = Server.getScatter().getAccountInfo();

			String sign = Eos.sign(bs, ai.privateKey);
			callback.onCallback(sign);
		});
	}

	private void apiRequestSignature(JSONObject data, Callback callback) {
		JSONObject payload = data.optJSONObject("payload");
		Object buf = payload.optJSONObject("buf").opt("data");

		List<Action> actions = new ArrayList<>();

		try {
			JSONArray acts = payload.optJSONObject("transaction").optJSONArray("actions");

			for (int i = 0; i < acts.length(); i++) {
				JSONObject act = acts.optJSONObject(i);

				Action action = new Action();
				action.account = act.optString("account");
				action.name = act.optString("name");
				action.bin = act.optString("data");

				JSONObject result = Eos.abiBinToJson(action.account, action.name, action.bin);
				if (result != null) {
					action.json = result.optJSONObject("args");
				}

				actions.add(action);
			}
		} catch (Exception e) {
			Log.e(Util.TAG, "get transaction", e);
		}

		requestSignature(buf, actions, (result) -> {
			if (result != null) {
				JSONObject ret = new JSONObject();
				ret.put("signatures", result);
				ret.put("requiredFields", payload.opt("requiredFields"));

				callback.onCallback(ret);
			} else {
				callback.onCallback(false);
			}
		});
	}

	private void apiRequestArbitrarySignature(JSONObject data, Callback callback) {
		Object message = data.optJSONObject("payload").opt("data");

		requestSignature(message, null, (result) -> {
			if (result != null) {
				callback.onCallback(result);
			} else {
				callback.onCallback(false);
			}
		});
	}

	private void apiAuthenticate(JSONObject data, Callback callback) throws Exception {
		JSONObject payload = data.optJSONObject("payload");
		String nonce = payload.optString("nonce");
		String dt = payload.optString("data");
		String origin = payload.optString("origin");

		if (dt == null) {
			dt = origin;
		}

		AccountInfo ai = Server.getScatter().getAccountInfo();

		String msg = Hash.sha256ToHex(dt) + Hash.sha256ToHex(nonce);
		String result = Eos.sign(msg.getBytes(), ai.privateKey);

		callback.onCallback(result);
	}
}
