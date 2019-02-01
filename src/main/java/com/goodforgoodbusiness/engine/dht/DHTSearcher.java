package com.goodforgoodbusiness.engine.dht;

import static java.util.stream.Stream.empty;

import java.security.InvalidKeyException;
import java.util.Optional;
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
	
	private final ClaimStore store;
	private final DHT dht;
	private final DHTAccessGovernor governor;
	private final PointerCrypter crypter;
	
	@Inject
	public DHTSearcher(ClaimStore store, DHT dht, DHTAccessGovernor governor, PointerCrypter crypter) {
		this.store = store;
		this.dht = dht;
		this.governor = governor;
		this.crypter = crypter;
	}
	
	public Stream<StoredClaim> search(Triple triple) {
		if (governor.allow(triple)) {
			log.info("DHT searching for " + triple);
			var pattern = Pattern.forSearch(triple);
			
			// pointer -> encrypted claim -> stored claim
			// check for nulls at each stage (not errors)
			return dht.getPointers(pattern)
				.parallel()
				.map(dhtPointer ->
					decryptPointer(triple, dhtPointer.getData())
						.filter(pointer -> !store.contains(pointer.getClaimId())) // only fetch if not known
						.flatMap(pointer -> fetchClaim(pointer, dhtPointer.getMeta()))
				)
				.filter(Optional::isPresent)
				.map(Optional::get)
			;
		}
		else {
			log.debug("DHT governer deny on " + triple + " (recently accessed)");
			return empty();
		}
	}
	
	private Optional<Pointer> decryptPointer(Triple triple, String data) {			
		log.debug("Decrypting pointer: " + data.substring(0, 10) + "...");
		
		try {
			var result = crypter.decrypt(triple, data);
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
