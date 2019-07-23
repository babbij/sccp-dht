package com.goodforgoodbusiness.engine.backend;

import java.util.stream.Stream;

/**
 * Basic implementable DHT backend for storing pointers
 */
public interface Warp {
	/**
	 * Publish a pointer to the index against a pattern.
	 */
	public void publishPointer(String pattern, byte[] data) throws WarpException;
	
	/**
	 * Searches for pointers with a specific pattern.
	 * Returns a Set of pointers that have been retrieved.
	 */
	public Stream<byte[]> searchForPointers(String pattern) throws WarpException;
}
