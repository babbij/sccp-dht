package com.goodforgoodbusiness.engine.crypto;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.engine.Pattern;
import com.goodforgoodbusiness.engine.crypto.primitive.key.EncodeableShareKey;
import com.goodforgoodbusiness.engine.store.keys.ShareKeyStore;
import com.goodforgoodbusiness.engine.store.keys.spec.ShareKeySpec;
import com.goodforgoodbusiness.engine.store.keys.spec.ShareRangeSpec;
import com.goodforgoodbusiness.kpabe.KPABEException;
import com.goodforgoodbusiness.kpabe.KPABEInstance;
import com.goodforgoodbusiness.model.Pointer;
import com.goodforgoodbusiness.shared.encode.JSON;
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
	
	public String encrypt(Pointer pointer, Set<String> attributes) throws KPABEException {
		var data = kpabe.encrypt(
			JSON.encodeToString(pointer),
			StringUtils.join(attributes, "|" )
		);
		
		return data;
	}
	
	public Pointer decrypt(Triple triple, String data) throws KPABEException, InvalidKeyException {
		return
			store.findKeys(new ShareKeySpec(triple))
				.map(EncodeableShareKey::toKeyPair)
				.map(keyPair -> {
					try {
						return KPABEInstance.decrypt(data, keyPair);
					}
					catch (KPABEException | InvalidKeyException e) {
						return null;
					}
				})
				.filter(Objects::nonNull)
				.findFirst()
				.map(json -> JSON.decode(json, Pointer.class))
				.orElse(null)
			;
	}
	
	public void saveShareKey(ShareKeySpec spec, KeyPair shareKey) {
		saveShareKey(spec, new EncodeableShareKey(shareKey));
	}
	
	public void saveShareKey(ShareKeySpec spec, EncodeableShareKey shareKey) {
		store.saveKey(spec, shareKey);
	}
	
	/**
	 * Create a share key.
	 * beg/end may be null for no limits on date/time.
	 */
	public EncodeableShareKey makeShareKey(ShareKeySpec spec, ShareRangeSpec range) throws KPABEException {
		var pattern = Pattern.forSpec(spec);
		
		if (range.getStart() != null) {
			pattern += " AND time >= " + range.getStart().toInstant().toEpochMilli() / 1000;
		}
		
		if (range.getEnd() != null) {
			pattern += " AND time <  " + range.getEnd().toInstant().toEpochMilli() / 1000;;
		}
		
		return new EncodeableShareKey(kpabe.shareKey(pattern));
	}
}
