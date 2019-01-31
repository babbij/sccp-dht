package com.goodforgoodbusiness.engine.dht.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.engine.dht.DHT;
import com.goodforgoodbusiness.engine.dht.DHTPointer;
import com.goodforgoodbusiness.engine.dht.DHTPointerMeta;
import com.goodforgoodbusiness.model.EncryptedClaim;
import com.goodforgoodbusiness.shared.encode.JSON;
import com.google.inject.Singleton;

@Singleton
public class MemDHT implements DHT {
	private static final Logger log = Logger.getLogger(MemDHT.class);
	
	// Mem doesn't need meta but we can use a single instance as a type check
	protected static final DHTPointerMeta META = new DHTPointerMeta();
	
	// store as String to test JSON encode/decode abilities 
	private Map<String, Set<String>> pointers = new HashMap<>();

	@Override
	public Stream<DHTPointer> getPointers(String pattern) {
		log.debug("Get pointers: " + pattern);
		
		return Optional
			.ofNullable(pointers.get(pattern))
			.map(Set::stream)
			.stream()
			.flatMap(stream -> stream.map(data -> new DHTPointer(data, META)))
		;
	}

	@Override
	public void putPointer(String pattern, String data) {
		log.debug("Put pointer: " + pattern);
		
		var existing = pointers.get(pattern);
		if (existing != null) {
			existing.add(data);
		}
		else {
			var newSet = new HashSet<String>();
			newSet.add(data);
			pointers.put(pattern, newSet);
		}
	}

	private Map<String, String> claims = new HashMap<>();
	
	@Override
	public Optional<EncryptedClaim> getClaim(String id, DHTPointerMeta meta) {
		log.debug("Get claim: " + id);
		
		if (meta == META) {
			return Optional
				.ofNullable(claims.get(id))
				.map(json -> JSON.decode(json, EncryptedClaim.class))
			;
		}
		else {
			return Optional.empty();
		}
	}

	@Override
	public void putClaim(EncryptedClaim claim) {
		log.debug("Put claim: " + claim.getId());
		claims.put(claim.getId(), JSON.encodeToString(claim));
	}

}
