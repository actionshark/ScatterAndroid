package com.linkto.main.core;

import android.util.Log;

import com.linkto.main.util.Util;
import com.linkto.main.util.HttpUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import io.eblock.eos4j.Ecc;
import io.eblock.eos4j.ecc.EccTool;

public class Eos {
	public static String privateToPublic(String privateKey) {
		try {
			return Ecc.privateToPublic(privateKey);
		} catch (Exception e) {
			Log.e(Util.TAG, "isValidPrivate", e);
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
			HttpUtil.Params params = new HttpUtil.Params();
			params.url = "https://mainnet.eosn.io/v2/state/get_key_accounts";
			params.method = "POST";
			params.headers.put("Content-Type", "application/json");

			JSONObject data = new JSONObject();
			data.put("public_key", publicKey);
			params.data = data.toString();

			HttpUtil.Result result = HttpUtil.request(params);
			if (result.code != 200) {
				return null;
			}

			JSONObject res = new JSONObject(result.content);
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
			HttpUtil.Params params = new HttpUtil.Params();
			params.url = "https://nodes.get-scatter.com/v1/chain/get_account";
			params.method = "POST";
			params.headers.put("Content-Type", "application/json");

			JSONObject data = new JSONObject();
			data.put("account_name", accountName);
			params.data = data.toString();

			HttpUtil.Result result = HttpUtil.request(params);

			Log.d(Util.TAG, "info: " + result.code + " " + result.content);

			if (result.code != 200) {
				return null;
			}

			return new JSONObject(result.content);
		} catch (Exception e) {
			Log.e(Util.TAG, "getKeyAccounts", e);
			return null;
		}
	}
}
