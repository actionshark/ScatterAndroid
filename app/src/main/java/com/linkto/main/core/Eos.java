package com.linkto.main.core;

import android.util.Log;

import com.linkto.main.util.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import io.eblock.eos4j.Ecc;
import io.eblock.eos4j.ecc.EccTool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Eos {
	public static String privateToPublic(String privateKey) {
		try {
			return Ecc.privateToPublic(privateKey);
		} catch (Exception e) {
			Log.e(Util.TAG, "privateToPublic", e);
			return null;
		}
	}

	public static String sign(byte[] data, String privateKey) {
		try {
			return EccTool.signHash(privateKey, data);
		} catch (Exception e) {
			Log.e(Util.TAG, "sign", e);
			return null;
		}
	}

	public static String getKeyAccounts(String publicKey) {
		try {
			JSONObject data = new JSONObject();
			data.put("public_key", publicKey);

			MediaType mediaType = MediaType.parse("application/json");
			RequestBody body = RequestBody.create(mediaType, data.toString());
			Request request = new Request.Builder()
					.url("https://mainnet.eosn.io/v2/state/get_key_accounts")
					.post(body)
					.build();

			Response response = new OkHttpClient().newCall(request).execute();

			if (!response.isSuccessful() || response.code() != 200) {
				return null;
			}

			JSONObject res = new JSONObject(response.body().string());
			JSONArray accounts = res.optJSONArray("account_names");
			if (accounts.length() > 0) {
				return accounts.getString(0);
			}

			return null;
		} catch (Exception e) {
			Log.e(Util.TAG, "getKeyAccounts", e);
			return null;
		}
	}

	public static JSONObject getAccount(String accountName) {
		try {
			JSONObject data = new JSONObject();
			data.put("account_name", accountName);

			MediaType mediaType = MediaType.parse("application/json");
			RequestBody body = RequestBody.create(mediaType, data.toString());
			Request request = new Request.Builder()
					.url("https://nodes.get-scatter.com/v1/chain/get_account")
					.post(body)
					.build();

			Response response = new OkHttpClient().newCall(request).execute();

			if (!response.isSuccessful() || response.code() != 200) {
				return null;
			}

			return new JSONObject(response.body().string());
		} catch (Exception e) {
			Log.e(Util.TAG, "getAccount", e);
			return null;
		}
	}
}
