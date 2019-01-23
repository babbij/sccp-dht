package com.goodforgoodbusiness.dhtjava.crypto.store.impl;

import static java.util.Collections.emptyList;
import static java.util.stream.Stream.concat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.goodforgoodbusiness.dhtjava.crypto.primitive.key.EncodeableShareKey;
import com.goodforgoodbusiness.dhtjava.crypto.store.ShareKeyIndex;
import com.goodforgoodbusiness.dhtjava.crypto.store.ShareKeyStore;
import com.google.inject.Singleton;

@Singleton
public class MemoryKeyStore implements ShareKeyStore {
	private final Map<String, List<EncodeableShareKey>> bySubject = new HashMap<>();
	private final Map<String, List<EncodeableShareKey>> byPredicate = new HashMap<>();
	private final Map<String, List<EncodeableShareKey>> byObject = new HashMap<>();
	
	@Override
	public void saveKey(ShareKeyIndex idx, EncodeableShareKey key) {
		if (idx.getSubject() != null) {
			var bySubjectIdx = bySubject.get(idx.getSubject());
			if (bySubjectIdx == null) {
				bySubjectIdx = new LinkedList<>();
				bySubject.put(idx.getSubject(), bySubjectIdx);
			}
			
			bySubjectIdx.add(key);
		}
		
		if (idx.getPredicate() != null) {
			var byPredicateIdx = byPredicate.get(idx.getPredicate());
			if (byPredicateIdx == null) {
				byPredicateIdx = new LinkedList<>();
				byPredicate.put(idx.getPredicate(), byPredicateIdx);
			}
			
			byPredicateIdx.add(key);
		}
		
		if (idx.getObject() != null) {
			var byObjectIdx = byObject.get(idx.getObject());
			if (byObjectIdx == null) {
				byObjectIdx = new LinkedList<>();
				byObject.put(idx.getObject(), byObjectIdx);
			}
			
			byObjectIdx.add(key);
		}
	}

	@Override
	public Stream<EncodeableShareKey> findKeys(ShareKeyIndex idx) {
		var seen = new HashSet<EncodeableShareKey>(); // suppress duplicates
		
		return 
			concat(
				bySubject.getOrDefault(idx.getSubject(), emptyList()).stream(),
				concat(
					byPredicate.getOrDefault(idx.getPredicate(), emptyList()).stream(),
					byObject.getOrDefault(idx.getObject(), emptyList()).stream()
				)
			)
			.filter(seen::add)
		;
	}

}
