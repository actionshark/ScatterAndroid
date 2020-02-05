package com.linkto.main.util;

import android.util.Log;

import java.security.MessageDigest;

public class SHA256 {
	public static byte[] hash(byte[] message) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			return messageDigest.digest(message);
		} catch (Exception e) {
			Log.e(Util.TAG, "SHA256.hash", e);
			return null;
		}
	}

	public static byte[] hash(String message) {
		return hash(message.getBytes());
	}

	public static String hashToHex(byte[] message) {
		byte[] bs = hash(message);
		return Util.bytesToHex(bs);
	}

	public static String hashToHex(String message) {
		byte[] bs = hash(message.getBytes());
		return Util.bytesToHex(bs);
	}
}
