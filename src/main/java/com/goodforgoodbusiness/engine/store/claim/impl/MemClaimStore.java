package com.goodforgoodbusiness.engine.store.claim.impl;

import static java.util.Collections.emptySet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.engine.Pattern;
import com.goodforgoodbusiness.engine.store.claim.ClaimStore;
import com.goodforgoodbusiness.model.StoredClaim;
import com.google.inject.Singleton;

@Singleton
public class MemClaimStore implements ClaimStore {
	private final Map<String, Set<StoredClaim>> storedClaims = new HashMap<>();
	
	@Override
	public void save(StoredClaim claim) {
		// store triples in local store.
		// we can recalculate the patterns since the claim is fully unencrypted.
		
		claim
			.getTriples()
			.flatMap(Pattern::forPublish)
			.forEach(pattern -> { 
				synchronized (storedClaims) {
					if (storedClaims.containsKey(pattern)) {
						storedClaims.get(pattern).add(claim);
					}
					else {
						var set = new HashSet<StoredClaim>();
						set.add(claim);
						storedClaims.put(pattern, set);
					}
				}
			});
		;
	}

	@Override
	public Stream<StoredClaim> search(Triple triple) {
		synchronized (storedClaims) {
			return storedClaims
				.getOrDefault(Pattern.forSearch(triple), emptySet())
				.stream()
			;
		}
	}
}
