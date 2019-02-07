package com.goodforgoodbusiness.engine.crypto;

import com.goodforgoodbusiness.kpabe.key.KPABEPublicKey;
import com.goodforgoodbusiness.kpabe.key.KPABESecretKey;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/** Will eventually deal with key rotation and so on for KP-ABE. for now it's pretty dumb. */
@Singleton
public class KeyManager {
	private final KPABEPublicKey publicKey;
	private final KPABESecretKey secretKey;
	
	public KeyManager(KPABEPublicKey publicKey, KPABESecretKey secretKey) {
		this.publicKey = publicKey;
		this.secretKey = secretKey;
	}
	
	@Inject
	public KeyManager(@Named("kpabe.publicKey") String publicKey, @Named("kpabe.secretKey") String secretKey) {
		this(new KPABEPublicKey(publicKey), new KPABESecretKey(secretKey));
	}
	
	public KPABEPublicKey getPublicKey() {
		return publicKey;
	}
	
	public KPABESecretKey getSecretKey() {
		return secretKey;
	}
}
