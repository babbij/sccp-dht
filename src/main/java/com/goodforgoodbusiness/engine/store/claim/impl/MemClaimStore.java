package com.goodforgoodbusiness.engine.store.claim.impl;

import static java.util.Collections.emptySet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.goodforgoodbusiness.engine.store.claim.ClaimStore;
import com.goodforgoodbusiness.model.StoredClaim;
import com.goodforgoodbusiness.model.TriTuple;
import com.google.inject.Singleton;

@Singleton
public class MemClaimStore implements ClaimStore {
	private final Set<String> storedIds = new HashSet<>();
	private final Map<TriTuple, Set<StoredClaim>> storedClaims = new HashMap<>();
	
	@Override
	public boolean contains(String claimId) {
		return storedIds.contains(claimId);
	}
	
	@Override
	public void save(StoredClaim claim) {
		synchronized (storedClaims) {
			// store triples in local store.
			// we can recalculate the patterns since the claim is fully unencrypted.
			
			claim.getTriples()
				.flatMap(triple -> TriTuple.from(triple).matchingCombinations())
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
			
			storedIds.add(claim.getId());
		}
	}

	@Override
	public Stream<StoredClaim> search(TriTuple tt) {
		synchronized (storedClaims) {
			return storedClaims
				.getOrDefault(tt, emptySet())
				.stream()
			;
		}
	}
}
