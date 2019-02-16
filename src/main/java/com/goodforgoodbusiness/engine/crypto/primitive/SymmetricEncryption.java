package com.goodforgoodbusiness.engine.crypto.primitive;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.goodforgoodbusiness.engine.crypto.primitive.key.EncodeableKeyException;
import com.goodforgoodbusiness.engine.crypto.primitive.key.EncodeableSecretKey;

public class SymmetricEncryption {
	public static final int KEY_LENGTH = 192;
	public static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5PADDING";

	private static final SecureRandom RANDOM;
	
	static {
		try {
			RANDOM = SecureRandom.getInstanceStrong();
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static EncodeableSecretKey createKey() {
		try {
			var keyGen = KeyGenerator.getInstance(EncodeableSecretKey.KEY_ALGORITHM);
			keyGen.init(KEY_LENGTH);
			
			return new EncodeableSecretKey(keyGen.generateKey());
		}
		catch (EncodeableKeyException | NoSuchAlgorithmException e) {
			throw new RuntimeException("Unexpected crypto error", e);
		}
	}
	
	public static EncodeableSecretKey createKeyFrom(byte [] input) {
		try {
			return new EncodeableSecretKey(new SecretKeySpec(input, EncodeableSecretKey.KEY_ALGORITHM));
		}
		catch (EncodeableKeyException e) {
			throw new RuntimeException("Unexpected crypto error", e);
		}
	}
	
	public static String encrypt(String inputString, SecretKey key) throws EncryptionException {
		try {
	        var cipher = Cipher.getInstance(CIPHER_ALGORITHM);
	        
	        var bInput = inputString.getBytes("UTF-8");    
	        var bVector = new byte[cipher.getBlockSize()];
	        RANDOM.nextBytes(bVector);
	        
	        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(bVector));
	        var bOutput = cipher.doFinal(bInput);
	        
	        var bResult = new byte [ bVector.length + bOutput.length ];
	        
	        System.arraycopy(bVector, 0, bResult, 0, bVector.length);
	        System.arraycopy(bOutput, 0, bResult, bVector.length, bOutput.length);
	        
	        return new String(Base64.getEncoder().encode(bResult), "UTF-8");
		}
		catch (InvalidKeyException e) {
			throw new EncryptionException("Invalid key", e);
		}
		catch (GeneralSecurityException | UnsupportedEncodingException e) {
			throw new RuntimeException("Unhandled error in Cipher", e);
		}
	}


	public static String decrypt(String inputString, SecretKey key) throws EncryptionException {
	    try {
	    	var bInput = Base64.getDecoder().decode( inputString.getBytes("UTF-8") );
	        var cipher = Cipher.getInstance(CIPHER_ALGORITHM);
	        var szBlock = cipher.getBlockSize();
	        
	        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(bInput, 0, szBlock));
	
	        var bOutput = cipher.doFinal(bInput, szBlock, bInput.length - szBlock);
	        
	        return new String(bOutput, "UTF-8");
	    }
	    catch (IllegalBlockSizeException e) {
	    	throw new EncryptionException("Bad cipher input", e);
	    }
		catch (GeneralSecurityException | UnsupportedEncodingException e) {
			throw new RuntimeException("Unhandled error in Cipher", e);
		}
	}
}
