package com.linkto.main.util;

import android.util.Log;

import java.security.MessageDigest;

public class Hash {
	public static byte[] sha256(String message) {
		return sha256(message.getBytes());
	}

	public static byte[] sha256(byte[] message) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			return messageDigest.digest(message);
		} catch (Exception e) {
			Log.e(Util.TAG, "sha256", e);
			return null;
		}
	}

	public static String sha256ToHex(String message) {
		return sha256ToHex(message.getBytes());
	}

	public static String sha256ToHex(byte[] message) {
		byte[] bs = sha256(message);
		return Util.bytesToHex(bs);
	}
}
