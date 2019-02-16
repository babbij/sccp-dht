package com.goodforgoodbusiness.engine.dht;

import static java.util.stream.Stream.empty;

import java.security.InvalidKeyException;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.engine.PatternMaker;
import com.goodforgoodbusiness.engine.crypto.ContainerCrypter;
import com.goodforgoodbusiness.engine.crypto.pointer.PointerCrypter;
import com.goodforgoodbusiness.engine.crypto.primitive.EncryptionException;
import com.goodforgoodbusiness.engine.store.container.ContainerStore;
import com.goodforgoodbusiness.engine.store.keys.ShareKeyStore;
import com.goodforgoodbusiness.kpabe.KPABEException;
import com.goodforgoodbusiness.kpabe.key.KPABEPublicKey;
import com.goodforgoodbusiness.model.EncryptedPointer;
import com.goodforgoodbusiness.model.Pointer;
import com.goodforgoodbusiness.model.StoredContainer;
import com.goodforgoodbusiness.model.TriTuple;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DHTSearcher {
	private static final Logger log = Logger.getLogger(DHTSearcher.class);
	
	private final ShareKeyStore keyStore;
	private final ContainerStore containerStore;
	private final DHT dht;
	private final DHTAccessGovernor governor;
	private final PointerCrypter crypter;
	
	@Inject
	public DHTSearcher(ShareKeyStore keyStore, ContainerStore containerStore, DHT dht, DHTAccessGovernor governor, PointerCrypter crypter) {
		this.keyStore = keyStore;
		this.containerStore = containerStore;
		this.dht = dht;
		this.governor = governor;
		this.crypter = crypter;
	}
	
	public Stream<StoredContainer> search(TriTuple tuple, boolean save) {
		if (governor.allow(tuple)) {
			log.info("DHT searching for " + tuple);
			
			// look for anyone who's ever shared a key matching these triples with us
			// (possibly more than one) and fetch containers for each of them from the DHT
			return keyStore
				.knownInfoCreators(tuple)
				.distinct()
				.flatMap(infoCreator -> search(infoCreator, tuple, save))
			;
		}
		else {
			if (log.isDebugEnabled()) log.debug("DHT governer deny on " + tuple + " (recently accessed)");
			return empty();
		}
	}
	
	/** Fetch containers from a specific information creator */
	private Stream<StoredContainer> search(KPABEPublicKey infoCreator, TriTuple tuple, boolean save) {
		var patternHash = PatternMaker.forSearch(infoCreator, tuple);
		var containers = new HashSet<StoredContainer>();
		
		log.debug("DHT found infoCreator " + infoCreator.toString().substring(0, 10) + "...");
		log.debug("DHT searching for pointer patterns " + patternHash.substring(0,  10) + "...");
		
		// have to process this stream because state is changed
		dht.getPointers(patternHash)
			.distinct()
			.forEach(encryptedPointer -> {
				decryptPointer(infoCreator, tuple, encryptedPointer.getData())
					.filter(pointer -> !containerStore.contains(pointer.getContainerId()))
					.flatMap(pointer -> fetchContainer(pointer, encryptedPointer))
					.ifPresent(container -> {
						if (save) {
							containerStore.save(container);
						}
						
						containers.add(container);
					});
				;
			})
		;
		
		log.debug("Results found = " + containers.size());
		return containers.parallelStream();
	}
	
	/** Decrypt a pointer - note that returning empty is not error, just means we can't (which is often normal). */
	private Optional<Pointer> decryptPointer(KPABEPublicKey infoCreator, TriTuple pattern, String data) {			
		log.debug("Decrypting pointer: " + data.substring(0, 10) + "...");
		
		try {
			var result = crypter.decrypt(infoCreator, pattern, data);
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
	
	private Optional<StoredContainer> fetchContainer(Pointer pointer, EncryptedPointer encryptedPointer) {
		log.debug("Fetching container: " + pointer.getContainerId());
		var encryptedContainerHolder = dht.getContainer(pointer.getContainerId(), encryptedPointer);
		
		if (encryptedContainerHolder.isPresent()) {
			var encryptedContainer = encryptedContainerHolder.get();
			log.debug("Decrypting container: " + encryptedContainer.getId());
			
			try {
				return Optional.of(new ContainerCrypter(pointer.getContainerKey()).decrypt(encryptedContainer));
			}
			catch (EncryptionException e) {
				log.error("Error decrypting container", e);
				return Optional.empty();
			}
		}
		else {
			return Optional.empty();
		}
	}
}
