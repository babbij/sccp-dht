package com.goodforgoodbusiness.engine.crypto.pointer;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.util.Objects;
import java.util.Optional;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.engine.crypto.primitive.key.EncodeableShareKey;
import com.goodforgoodbusiness.engine.store.keys.ShareKeyStore;
import com.goodforgoodbusiness.kpabe.KPABEException;
import com.goodforgoodbusiness.kpabe.key.KPABEPublicKey;
import com.goodforgoodbusiness.model.Pointer;
import com.goodforgoodbusiness.shared.encode.JSON;
import com.google.inject.Inject;

/**
 * Performs pointer encrypt/decrypt and maintains the keystore
 */
public abstract class PointerCrypter {
	private static final Logger log = Logger.getLogger(PointerCrypter.class);
	
	private final ShareKeyStore store;
	
	@Inject
	public PointerCrypter(ShareKeyStore store) {
		this.store = store;
	}
	
	/** Basic encryption function implemented by subclasses **/
	protected abstract String encrypt(String data, String attributes) throws KPABEException, InvalidKeyException;
	
	/** Basic decryption function implemented by subclasses 
	 * @throws InvalidKeyException **/
	protected abstract String decrypt(String data, KeyPair keyPair) throws KPABEException, InvalidKeyException;
	
	/** Basic sharekey function implemented by subclasses **/
	protected abstract KeyPair shareKey(String pattern) throws KPABEException, InvalidKeyException;
	
	/**
	 * Higher level encrypt function
	 */
	public String encrypt(Pointer pointer, String attributes) throws KPABEException, InvalidKeyException {
		return encrypt(JSON.encodeToString(pointer), attributes);
	}

	/**
	 * Higher level decrypt function
	 * Specify publicKey so we know which keys to try against the data
	 */
	public Optional<Pointer> decrypt(KPABEPublicKey publicKey, String data) throws KPABEException, InvalidKeyException {
		return
			store.keysForDecrypt(publicKey) // XXX think about expiry?
				.parallel()
				.map(EncodeableShareKey::toKeyPair)
				.map(keyPair -> {
					try {
						return decrypt(data, keyPair);
					}
					catch (KPABEException | InvalidKeyException e) {
						log.error("Error decrypting pointer", e);
						return null;
					}
				})
				.filter(Objects::nonNull)
				.findFirst()
				.map(json -> JSON.decode(json, Pointer.class))
			;
	}
}
