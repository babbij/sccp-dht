package com.goodforgoodbusiness.engine.crypto;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.jena.graph.Triple;
import org.apache.log4j.Logger;

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
	private static final Logger log = Logger.getLogger(PointerCrypter.class);
	
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
	
	public Optional<Pointer> decrypt(Triple triple, String data) throws KPABEException, InvalidKeyException {
		return
			store.findKeys(new ShareKeySpec(triple))
				.parallel()
				.map(EncodeableShareKey::toKeyPair)
				.map(keyPair -> {
					try {
						return Optional.ofNullable(KPABEInstance.decrypt(data, keyPair));
					}
					catch (KPABEException | InvalidKeyException e) {
						log.error("Error decrypting pointer", e);
						return Optional.<String>empty();
					}
				})
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst()
				.map(json -> JSON.decode(json, Pointer.class))
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
		var pattern = 
			Pattern.forSpec(spec)
			+ 
			Optional
				.ofNullable(range.getStart())
				.map(PointerCrypter::toEpochSecs)
				.map(epochsec -> " AND time >= " + epochsec)
				.orElse("")
			+
			Optional
				.ofNullable(range.getEnd())
				.map(PointerCrypter::toEpochSecs)
				.map(epochsec -> " AND time <  " + epochsec)
				.orElse("")
		;
		
		return new EncodeableShareKey(kpabe.shareKey(pattern));
	}
	
	private static long toEpochSecs(ZonedDateTime datetime) {
		return datetime.toInstant().toEpochMilli() / 1000;
	}
}
