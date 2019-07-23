package com.goodforgoodbusiness.engine;

import java.util.Optional;
import java.util.stream.Stream;

import io.vertx.core.Future;

/**
 * Basic implementable DHT backend for storing pointers & containers.
 */
public interface DHTBackend {
	/**
	 * Publish a pointer to the index against a pattern.
	 */
	public void publishPointer(String pattern, byte[] data, Future<Void> future);
	
	/**
	 * Searches for pointers with a specific pattern.
	 * Returns a Set of pointers that have been retrieved.
	 */
	public void searchForPointers(String pattern, Future<Stream<byte[]>> future);
	
	/**
	 * Publish a container so others may access it.
	 * Yields the container location URI.
	 */
	public void publishContainer(String id, byte[] data, Future<String> future);
	
	/**
	 * Search for a container by its ID.
	 * This will yield various locations that it is available to download.
	 */
	public void searchForContainer(String id, Future<Stream<String>> future);
	
	/**
	 * Fetches container data based on its location as returned 
	 * from pointer search operation. 
	 */
	public void fetchContainer(String location, Future<Optional<byte[]>> future);
}
