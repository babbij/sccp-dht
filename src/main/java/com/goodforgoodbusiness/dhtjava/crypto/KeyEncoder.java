package com.goodforgoodbusiness.dhtjava.crypto;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class KeyEncoder {
	public static String encodeKey(Key key) {
		try {
			return new String(Base64.getEncoder().encode(key.getEncoded()), "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException("JRE does not support UTF-8?!", e);
		}
	}
	
	public static SecretKey decodeSecretKey(String base64) throws CryptoException {
		return new SecretKeySpec(
			Base64.getDecoder().decode(base64.getBytes()),
			Symmetric.KEY_ALGORITHM
		);
	}
	
	public static PrivateKey decodePrivateKey(String base64) throws CryptoException {
		try {
			var keySpec = new PKCS8EncodedKeySpec(
				Base64.getDecoder().decode(base64.getBytes())
			);
			
			KeyFactory kf = KeyFactory.getInstance(Signing.KEY_ALGORITHM);
	        return kf.generatePrivate(keySpec);
		}
		catch (InvalidKeySpecException e) {
			throw new CryptoException("Invalid key", e);
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Algorithm not registered", e);
		}
	}
	
	public static PublicKey decodePublicKey(String base64) throws CryptoException {
		try {
			var keySpec = new X509EncodedKeySpec(
				Base64.getDecoder().decode(base64.getBytes())
			);
			
			KeyFactory kf = KeyFactory.getInstance(Signing.KEY_ALGORITHM);
	        return kf.generatePublic(keySpec);
		}
		catch (InvalidKeySpecException e) {
			throw new CryptoException("Invalid key", e);
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Algorithm not registered", e);
		}
	}
}
