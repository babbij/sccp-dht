package com.goodforgoodbusiness.dhtjava.dht.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.dhtjava.dht.DHTStore;
import com.goodforgoodbusiness.shared.JSON;
import com.goodforgoodbusiness.shared.model.EncryptedClaim;

public class MemoryDHTStore implements DHTStore {
	private static final Logger log = Logger.getLogger(MemoryDHTStore.class);
	
	private Map<String, Set<String>> pointers = new HashMap<>();

	@Override
	public Stream<String> getPointers(String pattern) {
		log.debug("Get pointers: " + pattern);
		
		var set = pointers.get(pattern);
		return (set != null) ? set.stream() : Stream.empty();
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
	public EncryptedClaim getClaim(String id) {
		log.debug("Get claim: " + id);
		var json = claims.get(id);
		return (json != null) ? JSON.decode(json, EncryptedClaim.class) : null;
	}

	@Override
	public void putClaim(EncryptedClaim claim) {
		log.debug("Put claim: " + claim.getId());
		claims.put(claim.getId(), JSON.encodeToString(claim));
	}

}
