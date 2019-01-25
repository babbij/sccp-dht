package com.goodforgoodbusiness.engine.dht;

import static com.google.common.collect.Streams.concat;
import static java.lang.System.currentTimeMillis;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Set;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.engine.Pattern;
import com.goodforgoodbusiness.engine.crypto.ClaimCrypter;
import com.goodforgoodbusiness.engine.crypto.PointerCrypter;
import com.goodforgoodbusiness.engine.crypto.primitive.EncryptionException;
import com.goodforgoodbusiness.kpabe.KPABEException;
import com.goodforgoodbusiness.model.Pointer;
import com.goodforgoodbusiness.model.StoredClaim;
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
	
	private final PointerCrypter pointerCrypter;
	private final DHT dht;
	
	@Inject
	public DHTPublisher(PointerCrypter pointerCrypter, DHT dht) {
		this.pointerCrypter = pointerCrypter;
		this.dht = dht;
	}
	
	public void publishClaim(StoredClaim claim) throws EncryptionException {
		log.info("Publishing claim: " + claim.getId());
		
		var crypter = new ClaimCrypter(); // with new symmetric key for claim
		var encryptedClaim = crypter.encrypt(claim);
		
		dht.putClaim(encryptedClaim);
		
		// generate combinations for pointers
		var patterns = claim
			.getTriples()
			.map(Pattern::forPublish)
			.flatMap(Set::stream)
			.collect(toSet())
		;
		
		// create a pointer for each pattern
		// the pointer needs to be encrypted with _all_ the possible patterns + other attributes
		var attributes = buildAttributes(patterns);
		
		patterns.forEach(pattern -> {
			try {
				var pointer = new Pointer(claim.getId(), crypter.getSecretKey().toEncodedString(), RANDOM.nextLong());
				publishPointer(pattern, pointer, attributes);
			}
			catch (EncryptionException e) {
				log.error(e);
			}
		});
	}
	
	private void publishPointer(String pattern, Pointer pointer, Set<String> attributes) throws EncryptionException {
		log.info("Publishing pattern: " + pattern);
		
		try {
			dht.putPointer(
				pattern,
				pointerCrypter.encrypt(
					pointer,
					attributes
				)
			);
		}
		catch (KPABEException e) {
			throw new EncryptionException("KPABE failure", e);
		}
	}
	
	/**
	 * Attributes to use for encryption are patterns + timestamp.
	 * This means keys can be issued with a pattern + a timestamp range to restrict access to a particular temporal window.
	 */
	private static Set<String> buildAttributes(Set<String> patterns) {
		return
			concat(
				patterns.stream(),
				of("time = " + Long.toString(currentTimeMillis() / 1000L))
			)
			.collect(toSet())
		;
	}
}
