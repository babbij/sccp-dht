package com.goodforgoodbusiness.engine.store.claim;

import com.goodforgoodbusiness.model.StoredClaim;

public interface ClaimStore {
	/**
	 * Store a claim we've found locally 
	 */
	public void save(StoredClaim claim);
}
