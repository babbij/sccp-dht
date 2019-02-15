package com.goodforgoodbusiness.engine.crypto;

import java.security.InvalidKeyException;
import java.time.ZonedDateTime;
import java.util.Optional;

import com.goodforgoodbusiness.engine.AttributeMaker;
import com.goodforgoodbusiness.engine.crypto.primitive.key.EncodeableShareKey;
import com.goodforgoodbusiness.kpabe.KPABEException;
import com.goodforgoodbusiness.kpabe.local.KPABELocalInstance;
import com.goodforgoodbusiness.model.TriTuple;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ShareKeyCreator {
	private final KeyManager keyManager;
	private final KPABELocalInstance kpabe;

	@Inject
	public ShareKeyCreator(KeyManager keyManager) throws KPABEException, InvalidKeyException {
		this.keyManager = keyManager;
		this.kpabe = KPABELocalInstance.forKeys(keyManager.getPublicKey(), keyManager.getSecretKey());
	}
	
	/**
	 * Create a share key.
	 * beg/end may be null for no limits on date/time.
	 */
	public EncodeableShareKey newKey(TriTuple pattern, Optional<ZonedDateTime> start, Optional<ZonedDateTime> end)
		throws KPABEException {
		
		return new EncodeableShareKey(
			kpabe.shareKey(
				AttributeMaker.forShare(keyManager.getPublicKey(), pattern, start, end)
			)
		);
	}
}
