package com.goodforgoodbusiness.engine.dht.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.engine.dht.DHT;
import com.goodforgoodbusiness.model.EncryptedContainer;
import com.goodforgoodbusiness.model.EncryptedPointer;
import com.goodforgoodbusiness.shared.encode.JSON;
import com.google.inject.Singleton;

@Singleton
public class MemDHT implements DHT {
	private static final Logger log = Logger.getLogger(MemDHT.class);
	
	// store as String to test JSON encode/decode abilities 
	private Map<String, Set<EncryptedPointer>> pointers = new HashMap<>();

	@Override
	public Stream<EncryptedPointer> getPointers(String pattern) {
		log.debug("Get pointers: " + pattern);
		
		return Optional
			.ofNullable(pointers.get(pattern))
			.stream()
			.flatMap(Set::stream)
		;
	}

	@Override
	public void putPointer(String pattern, EncryptedPointer pointer) {
		log.debug("Put pointer: " + pattern);
		
		var existing = pointers.get(pattern);
		if (existing != null) {
			existing.add(pointer);
		}
		else {
			var newSet = new HashSet<EncryptedPointer>();
			newSet.add(pointer);
			pointers.put(pattern, newSet);
		}
	}

	private Map<String, String> containers = new HashMap<>();
	
	@Override
	public Optional<EncryptedContainer> getContainer(String id, EncryptedPointer originalPointer) {
		log.debug("Get container: " + id);
		
		return Optional
			.ofNullable(containers.get(id))
			.map(json -> JSON.decode(json, EncryptedContainer.class))
		;
	}

	@Override
	public void putContainer(EncryptedContainer container) {
		log.debug("Put container: " + container.getId());
		containers.put(container.getId(), JSON.encodeToString(container));
	}

}
