package com.goodforgoodbusiness.dhtjava.dht.share;

import java.security.KeyPair;
import java.util.stream.Stream;

import org.apache.jena.graph.Triple;

public abstract class ShareKeyStore {	
	public final void saveKey(Triple triple, KeyPair shareKey) {		
		saveKey(new ShareKeyIndex(triple), new StoredShareKey(shareKey));
	}
	
	protected abstract void saveKey(ShareKeyIndex index, StoredShareKey storedKey);
	
	/**
	 * Find keys for a Triple search pattern.
	 * 
	 * The algorithm for this is to find any key that matches at least one of
	 * the concrete parts of the triple, as broader keys may grant access to
	 * more specific patterns, but also narrow keys may give partial access to
	 * narrower searches.  
	 */
	public final Stream<KeyPair> findKey(Triple triple) {
		return findKey(new ShareKeyIndex(triple)).map(storedKey -> storedKey.toKeyPair());
	}
	
	/**
	 * Find keys for a Triple search pattern.
	 * 
	 * The algorithm for this is to find any key that matches at least one of
	 * the concrete parts of the triple, as broader keys may grant access to
	 * more specific patterns, but also narrow keys may give partial access to
	 * narrower searches.  
	 */
	protected abstract Stream<StoredShareKey> findKey(ShareKeyIndex index);
}
