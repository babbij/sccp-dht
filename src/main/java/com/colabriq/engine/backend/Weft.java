package com.colabriq.engine.backend;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Basic implementable DHT backend for storing pointers & containers.
 */
public interface Weft {
	/**
	 * Publish a container so others may access it.
	 * Yields the container location URI.
	 */
	public String publishContainer(String id, byte[] data) throws WeftException;
	
	/**
	 * Search for a container by its ID.
	 * This will yield various locations that it is available to download.
	 */
	public Stream<String> searchForContainer(String id) throws WeftException;
	
	/**
	 * Fetches container data based on its location as returned 
	 * from pointer search operation. 
	 */
	public Optional<byte[]> fetchContainer(String location) throws WeftException;
}
