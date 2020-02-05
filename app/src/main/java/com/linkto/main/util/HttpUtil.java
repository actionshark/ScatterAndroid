package com.linkto.main.util;

import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class HttpUtil {
	private static String CHARSET = "UTF-8";

	public static class Params {
		public String url;
		public String method = "GET";

		public int connectTimeout = 5000;
		public int readTimeout = 5000;

		public final Map<String, String> headers = new HashMap<String, String>();
		public String data;
	}

	public static class Result {
		public int code = -1;
		public String content = "";
	}

	public interface ResultListener {
		void onResult(Result result);
	}

	public static Result request(Params params) {
		Result result = new Result();

		try {
			URL url = new URL(params.url);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setRequestMethod(params.method);
			conn.setConnectTimeout(params.connectTimeout);
			conn.setReadTimeout(params.readTimeout);

			for (Entry<String, String> entry : params.headers.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				conn.setRequestProperty(key, value);
			}

			conn.setDoInput(true);
			if ("POST".equals(conn.getRequestMethod()) || params.data != null) {
				conn.setDoOutput(true);
			}

			conn.connect();

			if (params.data != null) {
				byte[] bs = params.data.getBytes(CHARSET);

				OutputStream os = conn.getOutputStream();
				os.write(bs);
				os.flush();
				os.close();
			}

			result.code = conn.getResponseCode();
			if (result.code == HttpURLConnection.HTTP_OK) {
				InputStream is = conn.getInputStream();
				byte[] bs = new byte[1024 * 1024];
				int end = 0;

				for (int len; (len = is.read(bs, end, bs.length - end)) > 0; end += len);

				is.close();
				conn.disconnect();

				int start = 0;
				if (bs[0] == ((byte) 0xef) && bs[1] == ((byte) 0xbb) && bs[2] == ((byte) 0xbf)) {
					start = 3;
				}

				result.content = new String(bs, start, end - start, CHARSET);
			}
		} catch (Exception e) {
			Log.e(Util.TAG, "http",  e);
		}

		return result;
	}

	public static void requestAsync(final Params params, final ResultListener listener) {
		new Thread(() -> {
				Result result = request(params);
				if (listener != null) {
					listener.onResult(result);
				}
			}
		).start();
	}
}
