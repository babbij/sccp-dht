package com.goodforgoodbusiness.engine.crypto.key;

import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import com.goodforgoodbusiness.engine.crypto.AsymmetricEncryption;
import com.google.gson.annotations.JsonAdapter;

@JsonAdapter(EncodeableKey.Serializer.class)
public class EncodeablePublicKey extends AbstractEncodeableKey implements EncodeableKey, PublicKey, ECPublicKey {
	private static final long serialVersionUID = 1L;
	
	private static PublicKey unencode(String encodedForm) throws EncodeableKeyException {
		try {
			var spec = new X509EncodedKeySpec(Base64.getDecoder().decode(encodedForm.getBytes()));
			
			var kf = KeyFactory.getInstance(AsymmetricEncryption.KEY_ALGORITHM);
	        return kf.generatePublic(spec);
		}
		catch (InvalidKeySpecException e) {
			throw new EncodeableKeyException("Invalid key", e);
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Algorithm not registered", e);
		}
	}
	
	public EncodeablePublicKey(PublicKey key) {
		super(key);
	}
	
	public EncodeablePublicKey(String encodedForm) throws EncodeableKeyException {
		super(unencode(encodedForm));
	}
	
	@Override
	public ECParameterSpec getParams() {
		return ((ECPublicKey)key).getParams();
	}

	@Override
	public ECPoint getW() {
		return ((ECPublicKey)key).getW();
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
