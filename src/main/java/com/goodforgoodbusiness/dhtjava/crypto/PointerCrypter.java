package com.goodforgoodbusiness.dhtjava.crypto;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.dhtjava.Pattern;
import com.goodforgoodbusiness.dhtjava.crypto.primitive.key.EncodeableShareKey;
import com.goodforgoodbusiness.dhtjava.crypto.store.ShareKeyStore;
import com.goodforgoodbusiness.dhtjava.crypto.store.spec.ShareKeySpec;
import com.goodforgoodbusiness.dhtjava.crypto.store.spec.ShareRangeSpec;
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
	
	public String encrypt(Pointer pointer, List<String> attributes) throws KPABEException {
		var data = kpabe.encrypt(
			JSON.encodeToString(pointer),
			StringUtils.join(attributes, "|" )
		);
		
		return data;
	}
	
	public Pointer decrypt(Triple triple, String data) throws KPABEException, InvalidKeyException {
		return
//			store.findKeys(new ShareKeySpec(triple))
//				.map(EncodeableShareKey::toKeyPair)
			Stream.of(kpabe.shareKey(Pattern.forSearch(triple)))
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
