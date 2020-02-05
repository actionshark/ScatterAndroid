package com.linkto.main.core;

import android.content.Context;
import android.util.Log;
import android.webkit.WebView;

import com.linkto.main.util.SHA256;
import com.linkto.main.util.Util;
import com.linkto.main.util.HttpUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class Eos {
	private static ScriptEngine sScriptEngine;
	private static WebView sWebView;

	public static void init(Context context) {
		try {
			InputStream is = context.getAssets().open("eosjs-ecc.js");
			byte[] bs = new byte[is.available()];
			int offset = 0;

			for (int len; (len = is.read(bs, offset, bs.length - offset)) > 0; offset += len) ;

			String script = new String(bs, 0, offset);

			sScriptEngine = new ScriptEngineManager().getEngineByName("rhino");
			sScriptEngine.eval(script);
		} catch (Exception e) {
			Log.e(Util.TAG, "init", e);
		}
	}

	private static void setWebView(WebView webView) {
		sWebView = webView;
	}

	private static Object eval(String script, Object... args) {
		try {
			script = String.format(script, args);
			return sScriptEngine.eval(script);
		} catch (Exception e) {
			Log.e(Util.TAG, "eval", e);
			return null;
		}
	}

	public static boolean isValidPrivate(String privateKey) {
		try {
			return (boolean) eval("global.eosjs_ecc.isValidPrivate('%s')", privateKey);
		} catch (Exception e) {
			Log.e(Util.TAG, "isValidPrivate", e);
			return false;
		}
	}

	public static String privateToPublic(String privateKey) {
		try {
			return (String) eval("global.eosjs_ecc.privateToPublic('%s')", privateKey);
		} catch (Exception e) {
			Log.e(Util.TAG, "isValidPrivate", e);
			return null;
		}
	}

	public static String sign(byte[] data, String privateKey) {
		String hex = SHA256.hashToHex(data);

		return (String) eval("global.eosjs_ecc.signHash('%s', '%s', 'hex')",
				hex, privateKey);
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
