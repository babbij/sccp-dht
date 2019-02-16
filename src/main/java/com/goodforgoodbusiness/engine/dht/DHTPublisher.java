package com.goodforgoodbusiness.engine.dht;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.engine.AttributeMaker;
import com.goodforgoodbusiness.engine.PatternMaker;
import com.goodforgoodbusiness.engine.crypto.ClaimCrypter;
import com.goodforgoodbusiness.engine.crypto.KeyManager;
import com.goodforgoodbusiness.engine.crypto.pointer.PointerCrypter;
import com.goodforgoodbusiness.engine.crypto.primitive.EncryptionException;
import com.goodforgoodbusiness.kpabe.KPABEException;
import com.goodforgoodbusiness.model.Pointer;
import com.goodforgoodbusiness.model.StoredClaim;
import com.goodforgoodbusiness.model.TriTuple;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DHTPublisher {
	private static final Logger log = Logger.getLogger(DHTPublisher.class);
	
	private static final SecureRandom RANDOM;
	
	static {
		try {
			RANDOM = SecureRandom.getInstanceStrong();
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
	private final KeyManager keyManager;
	private final PointerCrypter pointerCrypter;
	private final DHT dht;
	
	@Inject
	public DHTPublisher(KeyManager keyManager, PointerCrypter pointerCrypter, DHT dht) {
		this.keyManager = keyManager;
		this.pointerCrypter = pointerCrypter;
		this.dht = dht;
	}
	
	public void publishClaim(StoredClaim claim) throws EncryptionException {
		log.debug("Publishing claim: " + claim.getId());
		
		var crypter = new ClaimCrypter(); // creates a new key
		var encryptedClaim = crypter.encrypt(claim);
		
		dht.putClaim(encryptedClaim);
		
		// generate combinations for pointers
		var patterns = claim
			.getTriples()
			.flatMap(t -> PatternMaker.forPublish(keyManager, TriTuple.from(t)))
		;
		
		// the pointer needs to be encrypted with _all_ the possible patterns + other attributes
		var attributes = AttributeMaker.forPublish(keyManager.getPublicKey(), claim.getTriples().map(t -> TriTuple.from(t)));
		
		// create + publish a pointer for each generated pattern
		patterns
			.forEach(pattern -> {
				try {
					var pointer = new Pointer(
						claim.getId(),
						crypter.getSecretKey().toEncodedString(),
						RANDOM.nextLong()
					);
					
					publishPointer(pattern, pointer, attributes);
				}
				catch (EncryptionException e) {
					log.error(e);
				}
			})
		;
	}
	
	private void publishPointer(String pattern, Pointer pointer, String attributes) throws EncryptionException {
		log.debug("Publishing pattern: " + pattern);
		
		try {
			dht.putPointer(
				pattern,
				pointerCrypter.encrypt(
					pointer,
					attributes
				)
			);
		}
		catch (KPABEException | InvalidKeyException e) {
			throw new EncryptionException("KPABE failure", e);
		}
	}
}
