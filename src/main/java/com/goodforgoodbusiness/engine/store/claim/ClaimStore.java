package com.goodforgoodbusiness.engine.store.claim;

import java.util.Optional;
import java.util.stream.Stream;

import com.goodforgoodbusiness.model.StoredClaim;
import com.goodforgoodbusiness.model.TriTuple;

/**
 * The local claim store, where we store our claims and others received

 */
public interface ClaimStore {
	/**
	 * Test if the store contains a claim ID already
	 */
	public boolean contains(String claimId);
	
	/**
	 * Get claim by ID
	 */
	public Optional<StoredClaim> getClaim(String claimId);
	
	/**
	 * Store a claim we've found locally 
	 */
	public void save(StoredClaim claim);

	/**
	 * Find claims for a Tri Tuple 
	 */
	public Stream<StoredClaim> search(TriTuple tt);
}
