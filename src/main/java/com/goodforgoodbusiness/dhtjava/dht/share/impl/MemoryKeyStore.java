package com.goodforgoodbusiness.dhtjava.dht.share.impl;

import static java.util.Collections.emptyList;
import static java.util.stream.Stream.concat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.goodforgoodbusiness.dhtjava.dht.share.ShareKeyIndex;
import com.goodforgoodbusiness.dhtjava.dht.share.ShareKeyStore;
import com.goodforgoodbusiness.dhtjava.dht.share.StoredShareKey;
import com.google.inject.Singleton;

@Singleton
public class MemoryKeyStore extends ShareKeyStore {
	private final Map<String, List<StoredShareKey>> bySubject = new HashMap<>();
	private final Map<String, List<StoredShareKey>> byPredicate = new HashMap<>();
	private final Map<String, List<StoredShareKey>> byObject = new HashMap<>();
	
	@Override
	public void saveKey(ShareKeyIndex idx, StoredShareKey storedKey) {
		if (idx.getSubject() != null) {
			var bySubjectIdx = bySubject.get(idx.getSubject());
			if (bySubjectIdx == null) {
				bySubjectIdx = new LinkedList<>();
				bySubject.put(idx.getSubject(), bySubjectIdx);
			}
			
			bySubjectIdx.add(storedKey);
		}
		
		if (idx.getPredicate() != null) {
			var byPredicateIdx = byPredicate.get(idx.getPredicate());
			if (byPredicateIdx == null) {
				byPredicateIdx = new LinkedList<>();
				byPredicate.put(idx.getPredicate(), byPredicateIdx);
			}
			
			byPredicateIdx.add(storedKey);
		}
		
		if (idx.getObject() != null) {
			var byObjectIdx = byObject.get(idx.getObject());
			if (byObjectIdx == null) {
				byObjectIdx = new LinkedList<>();
				byObject.put(idx.getObject(), byObjectIdx);
			}
			
			byObjectIdx.add(storedKey);
		}
	}

	@Override
	public Stream<StoredShareKey> findKey(ShareKeyIndex idx) {
		var seen = new HashSet<StoredShareKey>(); // suppress duplicates
		
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
