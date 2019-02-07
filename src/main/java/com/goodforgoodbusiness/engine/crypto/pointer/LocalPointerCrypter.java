package com.goodforgoodbusiness.engine.crypto.pointer;

import java.security.InvalidKeyException;
import java.security.KeyPair;

import com.goodforgoodbusiness.engine.crypto.KeyManager;
import com.goodforgoodbusiness.engine.store.keys.ShareKeyStore;
import com.goodforgoodbusiness.kpabe.KPABEException;
import com.goodforgoodbusiness.kpabe.local.KPABELocalInstance;
import com.google.inject.Inject;

/**
 * Performs pointer encrypt/decrypt and maintains the keystore
 */
public class LocalPointerCrypter extends PointerCrypter {
	private final KPABELocalInstance kpabe;
	
	@Inject
	public LocalPointerCrypter(KeyManager keyManager, ShareKeyStore store) throws InvalidKeyException {
		super(store);
		this.kpabe = KPABELocalInstance.forKeys(keyManager.getPublicKey(), keyManager.getSecretKey());
	}
	
	@Override
	protected String encrypt(String data, String attributes) throws KPABEException {
		return kpabe.encrypt(data, attributes);
	}
	
	@Override
	protected String decrypt(String data, KeyPair keyPair) throws KPABEException, InvalidKeyException {
		return KPABELocalInstance.decrypt(data, keyPair);
	}

	@Override
	protected KeyPair shareKey(String attributes) throws KPABEException {
		return kpabe.shareKey(attributes);
	}
}
