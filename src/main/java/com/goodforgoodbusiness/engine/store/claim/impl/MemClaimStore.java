package com.goodforgoodbusiness.engine.store.claim.impl;

import static java.util.Collections.emptySet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.goodforgoodbusiness.engine.store.claim.ClaimStore;
import com.goodforgoodbusiness.model.StoredClaim;
import com.goodforgoodbusiness.model.TriTuple;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MemClaimStore implements ClaimStore {
	private final Map<String, StoredClaim> claimsById;
	private final Map<TriTuple, Set<StoredClaim>> claimsByPattern;
	
	public MemClaimStore(Map<String, StoredClaim> claimsById, Map<TriTuple, Set<StoredClaim>> claimsByPattern) {
		this.claimsById = claimsById;
		this.claimsByPattern = claimsByPattern;
	}
	
	@Inject
	public MemClaimStore() {
		this(new HashMap<>(), new HashMap<>());
	}
	
	@Override
	public boolean contains(String claimId) {
		return claimsById.containsKey(claimId);
	}
	
	public Optional<StoredClaim> getClaim(String id) {
		return Optional.ofNullable(claimsById.get(id));
	}
	
	@Override
	public void save(StoredClaim claim) {
		synchronized (claimsByPattern) {
			// store triples in local store.
			// we can recalculate the patterns since the claim is fully unencrypted.
			
			claim.getTriples()
				.flatMap(triple -> TriTuple.from(triple).matchingCombinations())
				.forEach(pattern -> { 
					synchronized (claimsByPattern) {
						if (claimsByPattern.containsKey(pattern)) {
							claimsByPattern.get(pattern).add(claim);
						}
						else {
							var set = new HashSet<StoredClaim>();
							set.add(claim);
							claimsByPattern.put(pattern, set);
						}
					}
				});
			;
			
			claimsById.put(claim.getId(), claim);
		}
	}

	@Override
	public Stream<StoredClaim> search(TriTuple tt) {
		synchronized (claimsByPattern) {
			return claimsByPattern
				.getOrDefault(tt, emptySet())
				.stream()
			;
		}
	}
}
