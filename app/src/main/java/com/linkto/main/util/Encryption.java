package com.linkto.main.util;

import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {
	private static final String CIPHER_NAME = "AES";
	private static final String KEY_ALGORITHM = "AES";

	private static SecretKeySpec getKey(String key) throws Exception {
		MessageDigest messageDigest = MessageDigest.getInstance("MD5");
		byte[] bs = messageDigest.digest(key.getBytes());
		return new SecretKeySpec(bs, KEY_ALGORITHM);
	}

	public static String encode(String key, String content) {
		try {
			Cipher cipher = Cipher.getInstance(CIPHER_NAME);
			cipher.init(Cipher.ENCRYPT_MODE, getKey(key));
			byte[] bs = cipher.doFinal(content.getBytes());
			return Base64.encodeToString(bs, Base64.DEFAULT);
		} catch (Exception e) {
			Log.e(Util.TAG, "Encryption.encode", e);
			return null;
		}
	}

	public static String decode(String key, String content) {
		try {
			Cipher cipher = Cipher.getInstance(CIPHER_NAME);
			cipher.init(Cipher.DECRYPT_MODE, getKey(key));
			byte[] bs = Base64.decode(content, Base64.DEFAULT);
			byte[] result = cipher.doFinal(bs);
			return new String(result);
		} catch (Exception e) {
			Log.e(Util.TAG, "Encryption.decode", e);
			return null;
		}
	}
}
