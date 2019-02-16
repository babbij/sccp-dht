package com.goodforgoodbusiness.engine.store.container.impl;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.goodforgoodbusiness.engine.store.container.ContainerStore;
import com.goodforgoodbusiness.model.StoredContainer;
import com.goodforgoodbusiness.model.TriTuple;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;


/**
 * Caching wrapper for ContainerStore.
 */
@Singleton
public class CachingContainerStore implements ContainerStore {
	@BindingAnnotation @Target({ FIELD, PARAMETER, METHOD }) @Retention(RUNTIME)
	public @interface Underlying {}
	
	private final Cache<String, Optional<StoredContainer>> containersById;
	private final Cache<TriTuple, Set<StoredContainer>> containersByPattern;
	
	private final ContainerStore underlying;
	
	@Inject
	public CachingContainerStore(@Underlying ContainerStore underlying, @Named("containerstore.cache.duration") String cacheDuration) {
		this(underlying, Duration.parse(cacheDuration));
	}
	
	public CachingContainerStore(ContainerStore underlying, Duration cacheDuration) {
		this.underlying = underlying;
		
		this.containersById = CacheBuilder
			.newBuilder()
			.expireAfterWrite(cacheDuration)
			.maximumSize(1000)
			.build()
		;
		
		this.containersByPattern = CacheBuilder
			.newBuilder()
			.expireAfterWrite(cacheDuration)
			.maximumSize(5000)
			.build()
		;
	}

	@Override
	public void save(StoredContainer container) {
		underlying.save(container);
		
		container.getTriples()
			.flatMap(triple -> TriTuple.from(triple).matchingCombinations())
			.forEach(pattern -> containersByPattern.invalidate(pattern));
		;
		
		containersById.put(container.getId(), Optional.of(container));
	}

	@Override
	public Stream<StoredContainer> search(TriTuple tt) {
		try {
			Set<StoredContainer> set = containersByPattern.get(tt, () -> underlying.search(tt).collect(Collectors.toSet()));
			set.forEach(container -> containersById.put(container.getId(), Optional.of(container)));
			return set.parallelStream();
		}
		catch (ExecutionException ee) {
			throw new RuntimeException(ee);
		}
	}
	
	@Override
	public Optional<StoredContainer> getContainer(String id) {
		try {
			return containersById.get(id, () -> underlying.getContainer(id));
		}
		catch (ExecutionException ee) {
			throw new RuntimeException(ee);
		}
	}
	
	@Override
	public boolean contains(String containerId) {
		return getContainer(containerId).isPresent();
	}
}
