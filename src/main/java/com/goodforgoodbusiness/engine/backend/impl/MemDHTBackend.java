package com.goodforgoodbusiness.engine.backend.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.engine.backend.Warp;
import com.goodforgoodbusiness.engine.backend.Weft;
import com.google.inject.Singleton;

/** In memory implementation of DHTBackend - for testing */
@Singleton
public class MemDHTBackend implements Warp, Weft { 
	private static final Logger log = Logger.getLogger(MemDHTBackend.class);
	
	private Map<String, Set< byte[]>> pointers = new HashMap<>();

	@Override
	public void publishPointer(String hashPattern, byte[] data) {
		log.info("PUBLISH POINTER " + hashPattern.toString() + " -> " + data);
		
		pointers.computeIfAbsent(hashPattern, (k) -> new HashSet<>());
		pointers.get(hashPattern).add(data);
	}
	
	@Override
	public Stream<byte[]> searchForPointers(String hashPattern) {
		log.info("SEARCH FOR POINTERS " + hashPattern);
		
		return Optional
			.ofNullable(pointers.get(hashPattern))
			.stream() // stream so it's just non-empty Optionals
			.flatMap(Set::stream)
		;
	}

	private Map<String, Set<String>> containerIDs = new HashMap<>();
	private Map<String, byte[]> containerData = new HashMap<>();
	
	@Override
	public String publishContainer(String id, byte[] data) {
		log.info("PUBLISH CONTAINER " + id);
		
		var location = RandomStringUtils.random(20, true, true);
		
		containerData.put(location, data);
		containerIDs.computeIfAbsent(id, (k) -> new HashSet<>());
		containerIDs.get(id).add(location);
		
		return location;		
	}

	@Override
	public Stream<String> searchForContainer(String id) {
		log.info("SEARCH FOR CONTAINER " + id);
		
		return Optional
			.ofNullable(containerIDs.get(id))
			.stream() // stream so it's just non-empty Optionals
			.flatMap(Set::stream)
		;
	}

	@Override
	public Optional<byte[]> fetchContainer(String location) {
		log.info("FETCH CONTAINER " + location);
		return Optional.ofNullable(containerData.get(location));
	}
}
