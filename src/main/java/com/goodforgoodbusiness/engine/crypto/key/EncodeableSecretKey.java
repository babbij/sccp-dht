package com.goodforgoodbusiness.engine.crypto.key;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.google.gson.annotations.JsonAdapter;

@JsonAdapter(EncodeableKey.Serializer.class)
public class EncodeableSecretKey extends AbstractEncodeableKey implements EncodeableKey, SecretKey {
	private static final long serialVersionUID = 1L;
	
	public static final String KEY_ALGORITHM = "AES";
	
	private static SecretKey unencode(String encodedForm) {
		return new SecretKeySpec(Base64.getDecoder().decode(encodedForm.getBytes()), KEY_ALGORITHM);
	}
	
	public EncodeableSecretKey(SecretKey key) throws EncodeableKeyException {
		super(key);
		
		if (!key.getAlgorithm().equals(KEY_ALGORITHM)) {
			throw new EncodeableKeyException("Only " + KEY_ALGORITHM + " keys are supported");
		}
	}
	
	public EncodeableSecretKey(String encodedForm) {
		super(unencode(encodedForm));
	}
	
	@Override
	public String toEncodedString() {
		try {
			return new String(Base64.getEncoder().encode(getEncoded()), "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException("JRE does not support UTF-8?!", e);
		}
	}
}
