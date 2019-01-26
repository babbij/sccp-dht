package com.goodforgoodbusiness.engine.store.claim;

import java.util.stream.Stream;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.model.StoredClaim;

/**
 * The local claim store, where we store our claims and others received

 */
public interface ClaimStore {
	/**
	 * Test if the store contains a claim ID already
	 */
	public boolean contains(String claimId);
	
	/**
	 * Store a claim we've found locally 
	 */
	public void save(StoredClaim claim);

	/**
	 * Find claims for triples 
	 */
	public Stream<StoredClaim> search(Triple triple);
}
