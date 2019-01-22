package com.goodforgoodbusiness.dhtjava.keys;

import java.security.KeyPair;
import java.util.stream.Stream;

import org.apache.jena.graph.Triple;

public abstract class KeyStore {	
	public final void saveKey(Triple triple, KeyPair shareKey) {		
		saveKey(new KeyIndex(triple), new StoredKey(shareKey));
	}
	
	protected abstract void saveKey(KeyIndex index, StoredKey storedKey);
	
	/**
	 * Find keys for a Triple search pattern.
	 * 
	 * The algorithm for this is to find any key that matches at least one of
	 * the concrete parts of the triple, as broader keys may grant access to
	 * more specific patterns, but also narrow keys may give partial access to
	 * narrower searches.  
	 */
	public final Stream<KeyPair> findKey(Triple triple) {
		return findKey(new KeyIndex(triple)).map(storedKey -> storedKey.toKeyPair());
	}
	
	/**
	 * Find keys for a Triple search pattern.
	 * 
	 * The algorithm for this is to find any key that matches at least one of
	 * the concrete parts of the triple, as broader keys may grant access to
	 * more specific patterns, but also narrow keys may give partial access to
	 * narrower searches.  
	 */
	protected abstract Stream<StoredKey> findKey(KeyIndex index);
}
