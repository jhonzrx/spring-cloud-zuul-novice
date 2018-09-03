package com.rsoft.gw.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptUtil {
	public static String md5(String inputText) {
		return encrypt(inputText, "md5");
	}

	public static String sha(String inputText) {
		return encrypt(inputText, "sha-1");
	}

	private static String encrypt(String inputText, String algorithmName) {
		if (inputText == null || "".equals(inputText.trim())) {
			throw new IllegalArgumentException("Please input source.");
		}
		if (algorithmName == null || "".equals(algorithmName.trim())) {
			algorithmName = "md5";
		}
		String encryptText = null;
		try {
			MessageDigest m = MessageDigest.getInstance(algorithmName);
			m.update(inputText.getBytes("UTF8"));
			byte s[] = m.digest();
			return hex(s);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return encryptText;
	}

	private static String hex(byte[] arr) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < arr.length; ++i) {
			sb.append(Integer.toHexString((arr[i] & 0xFF) | 0x100).substring(1, 3));
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		String md51 = md5("1234567890");
		String md52 = md5("abcdef");
		System.out.println(md51 + "\n" + md52);
		System.out.println("md5 length: " + md51.length());

		String sha1 = sha("abcdefghijk");
		String sha2 = sha("abcdef");
		System.out.println(sha1 + "\n" + sha2);
		System.out.println("sha length: " + sha1.length());
	}

}