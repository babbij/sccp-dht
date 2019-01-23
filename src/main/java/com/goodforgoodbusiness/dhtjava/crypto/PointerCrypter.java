package com.goodforgoodbusiness.dhtjava.crypto;

import static java.util.stream.Collectors.joining;

import java.security.InvalidKeyException;
import java.util.stream.Stream;

import com.goodforgoodbusiness.dhtjava.crypto.store.ShareKeyStore;
import com.goodforgoodbusiness.kpabe.KPABEException;
import com.goodforgoodbusiness.kpabe.KPABEInstance;
import com.goodforgoodbusiness.shared.JSON;
import com.goodforgoodbusiness.shared.model.Pointer;
import com.google.inject.Inject;

/**
 * Performs pointer encrypt/decrypt and maintains the keystore
 */
public class PointerCrypter {
	private final KPABEInstance kpabe;
	private final ShareKeyStore store;
	
	@Inject
	public PointerCrypter(KPABEInstance kpabe, ShareKeyStore store) {
		this.kpabe = kpabe;
		this.store = store;
	}
	
	public String encrypt(Pointer pointer, Stream<String> attributes) throws KPABEException {
		return kpabe.encrypt(
			JSON.encodeToString(pointer),
			attributes.collect( joining( "|" ) )
		);
	}
	
	public Pointer decrypt(String data) throws KPABEException, InvalidKeyException {
		// find share keys from keystore
		// XXX create temporary key
		// this key should be able to decrypt the pointer
//		var shareKey = kpabe.shareKey("foo");
		
//		var decrypted = KPABEInstance.decrypt(data, shareKey);
//		if (decrypted != null) {
//			// successful decryption
//			return JSON.decode(decrypted, Pointer.class);
//		}
//		else {
//			// unsucessful decryption (normal!)
			return null;
//		}
	}
	
//	private void saveKey(Triple triple, KeyPair shareKey) {		
//		store.saveKey(new ShareKeyIndex(triple), new StoredShareKey(shareKey));
//	}
//	
//	private Stream<KeyPair> findKey(Triple triple) {
//		return store.findKey(new ShareKeyIndex(triple)).map(storedKey -> storedKey.toKeyPair());
//	}
}
