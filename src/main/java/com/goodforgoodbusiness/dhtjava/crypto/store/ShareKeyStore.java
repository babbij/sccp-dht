package com.goodforgoodbusiness.dhtjava.crypto.store;

import java.util.stream.Stream;

import com.goodforgoodbusiness.dhtjava.crypto.primitive.key.EncodeableShareKey;
import com.goodforgoodbusiness.dhtjava.crypto.store.spec.ShareKeySpec;

public interface ShareKeyStore {	
	public void saveKey(ShareKeySpec index, EncodeableShareKey key);
	
	/**
	 * Find keys for a Triple search pattern.
	 * 
	 * The algorithm for this is to find any key that matches at least one of
	 * the concrete parts of the triple, as broader keys may grant access to
	 * more specific patterns, but also narrow keys may give partial access to
	 * narrower searches.  
	 */
	public Stream<EncodeableShareKey> findKeys(ShareKeySpec spec);
}
