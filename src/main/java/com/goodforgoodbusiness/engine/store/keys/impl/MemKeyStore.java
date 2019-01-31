package com.goodforgoodbusiness.engine.store.keys.impl;

import static java.util.Collections.emptyList;
import static java.util.stream.Stream.concat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.goodforgoodbusiness.engine.crypto.primitive.key.EncodeableShareKey;
import com.goodforgoodbusiness.engine.store.keys.ShareKeyStore;
import com.goodforgoodbusiness.engine.store.keys.spec.ShareKeySpec;
import com.google.inject.Singleton;

@Singleton
public class MemKeyStore implements ShareKeyStore {
	private final Map<String, List<EncodeableShareKey>> bySubject = new HashMap<>();
	private final Map<String, List<EncodeableShareKey>> byPredicate = new HashMap<>();
	private final Map<String, List<EncodeableShareKey>> byObject = new HashMap<>();
	
	@Override
	public void saveKey(ShareKeySpec idx, EncodeableShareKey key) {
		if (idx.getSubjectX().isPresent()) {
			var bySubjectIdx = bySubject.get(idx.getSubjectX().get());
			if (bySubjectIdx == null) {
				bySubjectIdx = new LinkedList<>();
				bySubject.put(idx.getSubjectX().get(), bySubjectIdx);
			}
			
			bySubjectIdx.add(key);
		}
		
		if (idx.getPredicate().isPresent()) {
			var byPredicateIdx = byPredicate.get(idx.getPredicate().get());
			if (byPredicateIdx == null) {
				byPredicateIdx = new LinkedList<>();
				byPredicate.put(idx.getPredicate().get(), byPredicateIdx);
			}
			
			byPredicateIdx.add(key);
		}
		
		if (idx.getObject().isPresent()) {
			var byObjectIdx = byObject.get(idx.getObject().get());
			if (byObjectIdx == null) {
				byObjectIdx = new LinkedList<>();
				byObject.put(idx.getObject().get(), byObjectIdx);
			}
			
			byObjectIdx.add(key);
		}
	}

	@Override
	public Stream<EncodeableShareKey> findKeys(ShareKeySpec spec) {
		var seen = new HashSet<EncodeableShareKey>(); // suppress duplicates
		
		return 
			concat(
				bySubject.getOrDefault(spec.getSubjectX().get(), emptyList()).stream(),
				concat(
					byPredicate.getOrDefault(spec.getPredicate(), emptyList()).stream(),
					byObject.getOrDefault(spec.getObject(), emptyList()).stream()
				)
			)
			.filter(seen::add)
		;
	}

}
