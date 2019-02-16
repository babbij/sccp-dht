package com.goodforgoodbusiness.engine.store.container;

import java.util.Optional;
import java.util.stream.Stream;

import com.goodforgoodbusiness.model.StoredContainer;
import com.goodforgoodbusiness.model.TriTuple;

/**
 * The local container store, where we store our containers and others received

 */
public interface ContainerStore {
	/**
	 * Test if the store contains a container ID already
	 */
	public boolean contains(String containerId);
	
	/**
	 * Get container by ID
	 */
	public Optional<StoredContainer> getContainer(String containerId);
	
	/**
	 * Store a container we've found locally 
	 */
	public void save(StoredContainer container);

	/**
	 * Find containers for a Tri Tuple 
	 */
	public Stream<StoredContainer> search(TriTuple tt);
}
