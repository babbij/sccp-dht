package com.goodforgoodbusiness.engine.store.container.impl;

import static java.util.Collections.emptySet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.goodforgoodbusiness.engine.store.container.ContainerStore;
import com.goodforgoodbusiness.model.StoredContainer;
import com.goodforgoodbusiness.model.TriTuple;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MemContainerStore implements ContainerStore {
	private final Map<String, StoredContainer> containersById;
	private final Map<TriTuple, Set<StoredContainer>> containersByPattern;
	
	public MemContainerStore(Map<String, StoredContainer> containersById, Map<TriTuple, Set<StoredContainer>> containersByPattern) {
		this.containersById = containersById;
		this.containersByPattern = containersByPattern;
	}
	
	@Inject
	public MemContainerStore() {
		this(new HashMap<>(), new HashMap<>());
	}
	
	@Override
	public boolean contains(String containerId) {
		return containersById.containsKey(containerId);
	}
	
	public Optional<StoredContainer> getContainer(String id) {
		return Optional.ofNullable(containersById.get(id));
	}
	
	@Override
	public void save(StoredContainer container) {
		synchronized (containersByPattern) {
			// store triples in local store.
			// we can recalculate the patterns since the container is fully unencrypted.
			
			container.getTriples()
				.flatMap(triple -> TriTuple.from(triple).matchingCombinations())
				.forEach(pattern -> { 
					synchronized (containersByPattern) {
						if (containersByPattern.containsKey(pattern)) {
							containersByPattern.get(pattern).add(container);
						}
						else {
							var set = new HashSet<StoredContainer>();
							set.add(container);
							containersByPattern.put(pattern, set);
						}
					}
				});
			;
			
			containersById.put(container.getId(), container);
		}
	}

	@Override
	public Stream<StoredContainer> search(TriTuple tt) {
		synchronized (containersByPattern) {
			return containersByPattern
				.getOrDefault(tt, emptySet())
				.stream()
			;
		}
	}
}
