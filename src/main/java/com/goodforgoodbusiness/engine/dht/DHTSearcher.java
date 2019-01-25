package com.goodforgoodbusiness.engine.dht;

import java.security.InvalidKeyException;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.jena.graph.Triple;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.engine.Pattern;
import com.goodforgoodbusiness.engine.crypto.ClaimCrypter;
import com.goodforgoodbusiness.engine.crypto.PointerCrypter;
import com.goodforgoodbusiness.engine.crypto.primitive.EncryptionException;
import com.goodforgoodbusiness.engine.store.claim.ClaimStore;
import com.goodforgoodbusiness.kpabe.KPABEException;
import com.goodforgoodbusiness.model.Pointer;
import com.goodforgoodbusiness.model.StoredClaim;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DHTSearcher {
	private static final Logger log = Logger.getLogger(DHTSearcher.class);
	
	private final DHT dht;
	private final PointerCrypter pointerCrypter;
	private final ClaimStore claimStore;
	
	@Inject
	public DHTSearcher(ClaimStore claimStore, DHT dht, PointerCrypter pointerCrypter) {
		this.claimStore = claimStore;
		this.dht = dht;
		this.pointerCrypter = pointerCrypter;
	}
	
	public Stream<StoredClaim> search(Triple triple) {
		var pattern = Pattern.forSearch(triple);
		
		// pointer -> encrypted claim -> stored claim
		// check for nulls at each stage (not errors)
		return dht.getPointers(pattern)
			.map(data -> decryptPointer(triple, data))
			.filter(Objects::nonNull)
			.map(pointer -> fetchClaim(pointer))
			.filter(Objects::nonNull)
		;
	}
	
	private Pointer decryptPointer(Triple triple, String data) {			
		log.info("Decrypting pointer: " + data.substring(0, 10) + "...");
		
		try {
			var result = pointerCrypter.decrypt(triple, data);
			if (result != null) {
				return result;
			}
			else {
				log.info("Decryption failed");
				return null;
			}
		}
		catch (InvalidKeyException | KPABEException e) {
			// unsuccessful decryption returns null
			// if an Exception is thrown this is a legitimate error.
			log.error("Error decrypting pointer", e);
			return null;
		}
	}
	
	private StoredClaim fetchClaim(Pointer pointer) {
		log.info("Fetching claim: " + pointer.getClaimId());
		var encryptedClaim = dht.getClaim(pointer.getClaimId());
		
		if (encryptedClaim != null) {
			try {
				log.info("Decrypting claim: " + encryptedClaim.getId());
				var claim = new ClaimCrypter(pointer.getClaimKey()).decrypt(encryptedClaim);
				claimStore.save(claim);
				return claim;
			}
			catch (EncryptionException e) {
				log.error("Error decrypting claim", e); 
			}
		}
		
		return null;
	}
}
