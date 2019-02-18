package com.goodforgoodbusiness.engine.backend;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Basic implementable DHT backend for storing pointers & containers.
 */
public interface DHTBackend {
	/**
	 * Publish some data with some keywords it can be searched for with
	 * Returns a String representing where it's stored in the backing store.
	 * Can be different representations depending on backend.
	 */
	public String publish(Set<String> keywords, String data);
	
	/**
	 * Searches the backend with a keyword as specified to publish.
	 * Returns a Set of Strings representing things that can be retrieved.
	 * Representation is implementation-specific.
	 */
	public Stream<String> search(String keyword);
	
	/**
	 * Fetches published data based on its location as returned 
	 * from publish or search operations.
	 */
	public Optional<String> fetch(String location);
}
