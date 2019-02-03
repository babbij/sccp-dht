package com.goodforgoodbusiness.engine.crypto.pointer;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.time.ZonedDateTime;
import java.util.Objects;
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
import com.goodforgoodbusiness.model.Pointer;
import com.goodforgoodbusiness.shared.encode.JSON;
import com.google.inject.Inject;

/**
 * Performs pointer encrypt/decrypt and maintains the keystore
 * 
 * AAAAFqpvysBBBSscsTuDSN3W0iewt4ltcGsAAAGooQFZsgEEtLIBAA9+w9w//mZS2+6wHyuKSd6chKul+bEm8cpMbeJgy/5hGhpMR7VD1T1tX5MeioujTOY1m2O+KQdjwt8rh6wfXAsNcmWxJWvHWkqZQTBWPsUA61Bofuwa6/WUG3gyI7psCxtV25/FCIF+tBrec5JBVFaxfbBMsWTVRs+Hc8MC+915CvEkeUByUQ+GEc9vpWayJyf51TmpjBIgoFuspjc3RoAJ/AaEj5o8V97u+bG68jkw5zbQTlycmzcCzTaihV5PDBFo6QqZFSxmZXx3AfjSbinJCxqDqJBvUZ5ySp/8YFOaIGaqD88+FyLBBUXUZ9BNhmG5PUNbpcHQy+aaeIl2APKhAmcxoSSyoSECBvfiQNAe9IUL4W05vtelSFvll/xee//9A9wb7TYvbymhAmcyoUSzoUECDeXuGxKTAIJ0p+CCR7upwZbFInffpJoUGSBp7kkT3RILMCMgSDLqhmbi7AcTwM+MmARE65qTWml8wEfRNLM2DqEBa6ElHQAAACAT2XWTqzXsSaKepU5x/OH3ANwRRBcIAsq1yFM9frkkZg==
 * AAAAFqpvylyvLOQlZbS9k7K5r2MPfDRrZXkAAACLoQVEX2Zvb6EksqEhAyNsvsOWE2wz6xDaPhAed5cd4OOeItehqiKqD9H6yrI7oQVkX2Zvb6FEs6FBAwd3wOZap3tQs5N4WLrf3dfgC2uzESjHD5S4T0OPEhNKIJI7N5BhXWai9/8WMhzs96C+6LjcCkre+bxkmclN+eWhBWlucHV0oQgdAAAAA2Zvbw==
 */
public abstract class PointerCrypter {
	private static final Logger log = Logger.getLogger(PointerCrypter.class);
	
	private final ShareKeyStore store;
	
	@Inject
	public PointerCrypter(ShareKeyStore store) {
		this.store = store;
	}
	
	/** Basic encryption function implemented by subclasses **/
	protected abstract String encrypt(String data, String attributes) throws KPABEException;
	
	/** Basic decryption function implemented by subclasses 
	 * @throws InvalidKeyException **/
	protected abstract String decrypt(String data, KeyPair keyPair) throws KPABEException, InvalidKeyException;
	
	/** Basic sharekey function implemented by subclasses **/
	protected abstract KeyPair shareKey(String pattern) throws KPABEException;
	
	/**
	 * Higher level encrypt function
	 */
	public String encrypt(Pointer pointer, Set<String> attributes) throws KPABEException {
		var data = encrypt(
			JSON.encodeToString(pointer),
			StringUtils.join(attributes, "|" )
		);
		
		return data;
	}

	/**
	 * Higher level decrypt function
	 */
	public Optional<Pointer> decrypt(Triple triple, String data) throws KPABEException, InvalidKeyException {
		return
			store.findKeys(new ShareKeySpec(triple))
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
	
	/**
	 * Higher level create a share key.
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
		
		return new EncodeableShareKey(shareKey(pattern));
	}
	
	private static long toEpochSecs(ZonedDateTime datetime) {
		return datetime.toInstant().toEpochMilli() / 1000;
	}
	
	public void saveShareKey(ShareKeySpec spec, KeyPair shareKey) {
		saveShareKey(spec, new EncodeableShareKey(shareKey));
	}
	
	public void saveShareKey(ShareKeySpec spec, EncodeableShareKey shareKey) {
		store.saveKey(spec, shareKey);
	}
}
