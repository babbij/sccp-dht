package com.goodforgoodbusiness.engine.crypto.key;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import com.goodforgoodbusiness.engine.crypto.AsymmetricEncryption;
import com.google.gson.annotations.JsonAdapter;;

@JsonAdapter(EncodeableKey.Serializer.class)
public class EncodeablePrivateKey extends AbstractEncodeableKey implements EncodeableKey, PrivateKey, ECPrivateKey {
	private static PrivateKey unencode(String encodedForm) throws EncodeableKeyException {
		try {
			var spec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(encodedForm.getBytes()));
			var kf = KeyFactory.getInstance(AsymmetricEncryption.KEY_ALGORITHM);
	        return kf.generatePrivate(spec);
		}
		catch (InvalidKeySpecException e) {
			throw new EncodeableKeyException("Invalid key", e);
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Algorithm not registered", e);
		}
	}
	
	public EncodeablePrivateKey(PrivateKey key) {
		super(key);
	}
	
	public EncodeablePrivateKey(String encodedForm) throws EncodeableKeyException {
		this(unencode(encodedForm));
	}
	
	@Override
	public ECParameterSpec getParams() {
		return ((ECPrivateKey)key).getParams();
	}

	@Override
	public BigInteger getS() {
		return ((ECPrivateKey)key).getS();
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
