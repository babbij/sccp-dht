package com.goodforgoodbusiness.dhtjava.crypto.abe;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import com.goodforgoodbusiness.dhtjava.crypto.abe.ABELibrary.CKeyPair;
import com.goodforgoodbusiness.dhtjava.crypto.abe.ABELibrary.COutString;
import com.goodforgoodbusiness.dhtjava.crypto.abe.key.ABEKey;
import com.goodforgoodbusiness.dhtjava.crypto.abe.key.ABEPublicKey;
import com.goodforgoodbusiness.dhtjava.crypto.abe.key.ABESecretKey;
import com.goodforgoodbusiness.dhtjava.crypto.abe.key.ABEShareKey;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public class ABE {
	static {
		System.loadLibrary("sccp");
	}
	
	private static Object LOCK = new Object();
	
	private static ABELibrary LIBRARY = (ABELibrary) Native.load("sccp", ABELibrary.class);
	
	private static final int DECRYPTION_FAILED = 10000;
	
	private static void checkResult(int result) throws ABEException {
		if (result != 0) {
			throw ABEException.forResult(result);
		}
	}
	
	public static ABE newKeys() throws ABEException {
		synchronized (LOCK) {
			CKeyPair.ByReference keyPair = new CKeyPair.ByReference();
			
			int result = LIBRARY.newKeyPair(keyPair);
			checkResult(result);
			
			return new ABE(
				new ABEPublicKey(keyPair.getPublicKey()),
				new ABESecretKey(keyPair.getSecretKey())
			);
		}
	}
	
	public static ABE forKeys(PublicKey publicKey, SecretKey secretKey) throws InvalidKeyException {
		if ((publicKey instanceof ABEPublicKey) && (secretKey instanceof ABESecretKey)) {
			return new ABE((ABEPublicKey)publicKey, (ABESecretKey)secretKey);
		}
		else {
			throw new InvalidKeyException("Must be ABE keys");
		}
	}
	
	private final ABEPublicKey publicKey;
	private final ABESecretKey secretKey;
	
	private ABE(ABEPublicKey publicKey, ABESecretKey secretKey) {
		this.publicKey = publicKey;
		this.secretKey = secretKey;
	}
	
	public PublicKey getPublicKey() {
		return publicKey;
	}
	
	public SecretKey getSecretKey() {
		return secretKey;
	}
	
	private static Pointer cKeyPointer(ABEKey key) {
		String keyString = key.toString();
		
		Pointer keyPointer = new Memory(keyString.length() + 1);
		keyPointer.setString(0, keyString);
		
		return keyPointer;
	}
	
	private CKeyPair.ByReference cKeyPair() {
		CKeyPair.ByReference keyPair = new CKeyPair.ByReference();
		
		keyPair.szPublicKey = cKeyPointer(this.publicKey);
		keyPair.szSecretKey = cKeyPointer(this.secretKey);
		
		return keyPair;
	}
	
	public String encrypt(String cleartext, String attributes) throws ABEException {
		synchronized (LOCK) {
			try (var keyPair = cKeyPair()) {
				COutString.ByReference cipherText = new COutString.ByReference();
				
				int result = LIBRARY.encrypt(keyPair, attributes, cleartext, cipherText);
				checkResult(result);
				
				return cipherText.toString();
			}
		}
	}
	
	public KeyPair shareKey(String attributes) throws ABEException {
		synchronized (LOCK) {
			try (var keyPair = cKeyPair()) {
				COutString.ByReference shareKey = new COutString.ByReference();
				
				int result = LIBRARY.shareKey(keyPair, attributes, shareKey);
				checkResult(result);
				
				return new KeyPair(publicKey, new ABEShareKey(shareKey.toString()));
			}
		}
	}
	
	public static String decrypt(String ciphertext, KeyPair keys) throws ABEException, InvalidKeyException {
		synchronized (LOCK) {
			Key publicKey = keys.getPublic();
			Key shareKey = keys.getPrivate();
			
			if ((publicKey instanceof ABEPublicKey) && (shareKey instanceof ABEShareKey)) {
				COutString.ByReference clearText = new COutString.ByReference();
	
				int result = LIBRARY.decrypt(publicKey.toString(), ciphertext, shareKey.toString(), clearText);
				if (result == DECRYPTION_FAILED) {
					return null;
				}
				
				checkResult(result);
				return clearText.toString();
			}
			else {
				throw new InvalidKeyException("Must be ABE keys");
			}
		}
	}
}
