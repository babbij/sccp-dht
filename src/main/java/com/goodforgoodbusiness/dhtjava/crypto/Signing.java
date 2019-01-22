package com.goodforgoodbusiness.dhtjava.crypto;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;

public class Signing {
	public static final String KEY_ALGORITHM = "EC";
	public static final int KEY_LENGTH = 256;
	public static final String SIGNING_ALGORITHM = "SHA256withECDSA";
	
	private static final SecureRandom RANDOM;
	
	static {
		try {
			RANDOM = SecureRandom.getInstanceStrong();
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Could not create strong SecureRandom instance");
		}
	}
	
	public static KeyPair generateKeyPair() {
		try {
			KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
			gen.initialize(KEY_LENGTH, RANDOM);
			return gen.generateKeyPair();
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Could not create strong KeyPairGenerator instance");
		}
	}
	
	public static boolean verify(byte [] input, String signature, PublicKey key) throws CryptoException {
		try {
			Signature sign = Signature.getInstance(SIGNING_ALGORITHM);
			sign.initVerify(key);
			sign.update(input);
			return sign.verify(Base64.getDecoder().decode(signature.getBytes("UTF-8")));
		}
		catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			throw new RuntimeException("Unexpected signing Exception", e);
		}
		catch (InvalidKeyException | SignatureException e) {
			throw new CryptoException("Unable to sign", e);
		}
	}
	
	public static boolean verify(String input, String signature, PublicKey key) throws CryptoException {
		try {
			return verify(input.getBytes("UTF-8"), signature, key);
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unexpected signing Exception", e);
		}
	}
	
	public static String sign(byte [] input, PrivateKey key) throws CryptoException {
		try {
			Signature sign = Signature.getInstance(SIGNING_ALGORITHM);
			sign.initSign(key);
			sign.update(input);
			
			return new String(Base64.getEncoder().encode(sign.sign()), "UTF-8");
		}
		catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			throw new RuntimeException("Unexpected signing Exception", e);
		}
		catch (InvalidKeyException | SignatureException e) {
			throw new CryptoException("Unable to sign", e);
		}
	}
	
	public static String sign(String input, PrivateKey key) throws CryptoException {
		try {
			return sign(input.getBytes("UTF-8"), key);
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unexpected signing Exception", e);
		}
	}
}
