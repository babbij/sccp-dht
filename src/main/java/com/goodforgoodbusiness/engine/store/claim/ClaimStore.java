package com.goodforgoodbusiness.engine.store.claim;

import java.util.stream.Stream;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.model.StoredClaim;

public interface ClaimStore {
	/**
	 * Store a claim we've found locally 
	 */
	public void save(StoredClaim claim);

	/**
	 * Find claims for triples 
	 */
	public Stream<StoredClaim> search(Triple triple);
}
