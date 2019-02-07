package com.goodforgoodbusiness.engine.dht;

import static java.util.stream.Stream.empty;

import java.security.InvalidKeyException;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.engine.PatternMaker;
import com.goodforgoodbusiness.engine.crypto.ClaimCrypter;
import com.goodforgoodbusiness.engine.crypto.pointer.PointerCrypter;
import com.goodforgoodbusiness.engine.crypto.primitive.EncryptionException;
import com.goodforgoodbusiness.engine.store.claim.ClaimStore;
import com.goodforgoodbusiness.engine.store.keys.ShareKeyStore;
import com.goodforgoodbusiness.kpabe.KPABEException;
import com.goodforgoodbusiness.kpabe.key.KPABEPublicKey;
import com.goodforgoodbusiness.model.Pointer;
import com.goodforgoodbusiness.model.StoredClaim;
import com.goodforgoodbusiness.model.TriTuple;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DHTSearcher {
	private static final Logger log = Logger.getLogger(DHTSearcher.class);
	
	private final ShareKeyStore keyStore;
	private final ClaimStore claimStore;
	private final DHT dht;
	private final DHTAccessGovernor governor;
	private final PointerCrypter crypter;
	
	@Inject
	public DHTSearcher(ShareKeyStore keyStore, ClaimStore claimStore, DHT dht, DHTAccessGovernor governor, PointerCrypter crypter) {
		this.keyStore = keyStore;
		this.claimStore = claimStore;
		this.dht = dht;
		this.governor = governor;
		this.crypter = crypter;
	}
	
	public Stream<StoredClaim> search(TriTuple tuple) {
		if (governor.allow(tuple)) {
			log.info("DHT searching for " + tuple);
			
			// look for anyone who's ever shared a key matching these triples with us
			// (possibly more than one)
			var keys = keyStore.knownSharers(tuple);
			
			// fetch any pointers on the network from these sharers
			return keys.flatMap(publicKey -> 
					dht.getPointers(PatternMaker.forSearch(publicKey, tuple))
						// decrypt if we can (we may not be able to - this is fine)
						// use flatmap so we can get rid of any nulls (decryption failures)
						.flatMap(dhtPointer -> 
							decryptPointer(publicKey, dhtPointer.getData())
								.stream()
								// only fetch those claims we didn't see before
								.filter(pointer -> !claimStore.contains(pointer.getClaimId()))
								.flatMap(pointer -> fetchClaim(pointer, dhtPointer.getMeta()).stream())
						)
				)
			;
		}
		else {
			if (log.isDebugEnabled()) log.debug("DHT governer deny on " + tuple + " (recently accessed)");
			return empty();
		}
	}
	
	private Optional<Pointer> decryptPointer(KPABEPublicKey publicKey, String data) {			
		log.debug("Decrypting pointer: " + data.substring(0, 10) + "...");
		
		try {
			var result = crypter.decrypt(publicKey, data);
			if (result.isEmpty()) {
				log.debug("Decryption failed (safe)");
			}
			
			return result;
		}
		catch (InvalidKeyException | KPABEException e) {
			// unsuccessful decryption returns null
			// if an Exception is thrown this is a legitimate error.
			log.error("Error decrypting pointer", e);
			return Optional.empty();
		}
	}
	
	private Optional<StoredClaim> fetchClaim(Pointer pointer, DHTPointerMeta meta) {
		log.debug("Fetching claim: " + pointer.getClaimId());
		var encryptedClaimHolder = dht.getClaim(pointer.getClaimId(), meta);
		
		if (encryptedClaimHolder.isPresent()) {
			var encryptedClaim = encryptedClaimHolder.get();
			log.debug("Decrypting claim: " + encryptedClaim.getId());
			
			try {
				return Optional.of(new ClaimCrypter(pointer.getClaimKey()).decrypt(encryptedClaim));
			}
			catch (EncryptionException e) {
				log.error("Error decrypting claim", e);
				return Optional.empty();
			}
		}
		else {
			return Optional.empty();
		}
	}
}
