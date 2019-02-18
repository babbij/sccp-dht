package com.goodforgoodbusiness.engine;

import java.time.ZonedDateTime;
import java.util.Optional;

import com.goodforgoodbusiness.engine.crypto.key.EncodeableShareKey;
import com.goodforgoodbusiness.kpabe.KPABEException;
import com.goodforgoodbusiness.kpabe.key.KPABEPublicKey;
import com.goodforgoodbusiness.kpabe.key.KPABESecretKey;
import com.goodforgoodbusiness.kpabe.local.KPABELocalInstance;
import com.goodforgoodbusiness.model.TriTuple;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/** Will eventually deal with key rotation and so on for KP-ABE. for now it's pretty dumb. */
@Singleton
public class ShareManager {
	private final KPABEPublicKey publicKey;
	private final KPABESecretKey secretKey;
	
	public ShareManager(KPABEPublicKey publicKey, KPABESecretKey secretKey) {
		this.publicKey = publicKey;
		this.secretKey = secretKey;
	}
	
	@Inject
	public ShareManager(@Named("kpabe.publicKey") String publicKey, @Named("kpabe.secretKey") String secretKey) {
		this(new KPABEPublicKey(publicKey), new KPABESecretKey(secretKey));
	}
	
	public KPABEPublicKey getCreatorKey() {
		return publicKey;
	}	

	public KPABELocalInstance getCurrentABE() {
		return KPABELocalInstance.forKeys(publicKey, secretKey);
	}
	
	/**
	 * Create a share key.
	 * beg/end may be null for no limits on date/time.
	 */
	public EncodeableShareKey newShareKey(TriTuple pattern, Optional<ZonedDateTime> start, Optional<ZonedDateTime> end)
		throws KPABEException {
		
		var kpabe = getCurrentABE(); // XXX: will need to work out what key was in use during the time range
		
		return new EncodeableShareKey(
			kpabe.shareKey(
				Attributes.forShare(kpabe.getPublicKey(), pattern, start, end)
			)
		);
	}
}
