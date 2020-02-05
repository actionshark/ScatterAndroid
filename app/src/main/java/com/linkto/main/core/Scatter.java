package com.linkto.main.core;

import android.util.Log;

import com.linkto.main.util.Hash;
import com.linkto.main.util.Util;

import org.java_websocket.WebSocket;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;

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

	private String mAccountName;
	private String mPrivateKey;
	private String mPublicKey;

	public void setInfo(String accountName, String privateKey, String publicKey) {
		mAccountName = accountName;
		mPrivateKey = privateKey;
		mPublicKey = publicKey;
	}

	public void onMessage(WebSocket webSocket, String message) throws Exception {
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

	private void apiGetOrRequestIdentity(JSONObject data, Callback callback) throws Exception {
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
		account.put("name", mAccountName);
		account.put("authority", "active");
		account.put("blockchain", blockchain);
		account.put("publicKey", mPublicKey);

		JSONArray accounts = new JSONArray();
		accounts.put(account);


		JSONObject id = new JSONObject();
		id.put("name", mAccountName);
		id.put("publicKey", mPublicKey);
		id.put("accounts", accounts);

		callback.onCallback(id);
	}

	private void requestSignature(Object message, Callback callback) throws Exception {
		byte[] bs;

		if (message instanceof String) {
			bs = ((String) message).getBytes();
		} else if (message instanceof JSONArray) {
			JSONArray ja = (JSONArray) message;
			bs = new byte[ja.length()];
			for (int i = 0; i < bs.length; i++) {
				bs[i] = (byte) ja.optInt(i);
			}
		} else if (message instanceof byte[]) {
			bs = (byte[]) message;
		} else {
			throw new IllegalArgumentException("illegal argument");
		}

		String sign = Eos.sign(bs, mPrivateKey);
		callback.onCallback(sign);
	}

	private void apiRequestSignature(JSONObject data, Callback callback) throws Exception {
		JSONObject payload = data.optJSONObject("payload");
		Object buf = payload.optJSONObject("buf").opt("data");

		requestSignature(buf, (result) -> {
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

	private void apiRequestArbitrarySignature(JSONObject data, Callback callback) throws Exception {
		Object message = data.optJSONObject("payload").opt("data");

		requestSignature(message, (result) -> {
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

		String msg = Hash.sha256ToHex(dt) + Hash.sha256ToHex(nonce);
		String result = Eos.sign(msg.getBytes(), mPrivateKey);

		callback.onCallback(result);
	}
}
