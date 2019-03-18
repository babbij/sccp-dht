package com.goodforgoodbusiness.engine.backend.impl;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.stream.Collectors.toSet;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import com.goodforgoodbusiness.engine.backend.DHTBackend;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * A cache layer for retrieving containers in the collection representing what's loaded on to the DHT
 */
@Singleton
public class CachingDHTBackend implements DHTBackend {
	@BindingAnnotation @Target({ FIELD, PARAMETER, METHOD }) @Retention(RUNTIME)
	public @interface Underlying { /* no params */ }
	
	private final Cache<String, Set<String>> keysCache;
	private final Cache<String, Optional<String>> dataCache;
	
	private final DHTBackend backend;
	
	@Inject
	public CachingDHTBackend(@Underlying DHTBackend backend, Duration cacheDuration) {
		this.keysCache = CacheBuilder
			.newBuilder()
			.expireAfterWrite(cacheDuration)
			.maximumSize(5000)
			.build()
		;
		
		this.dataCache = CacheBuilder
			.newBuilder()
			.expireAfterWrite(cacheDuration)
			.maximumSize(1000)
			.build()
		;
		
		this.backend = backend;
	}
	
	@Override
	public Optional<String> publish(Set<String> keywords, String data) {
		var location = backend.publish(keywords, data);
		
		if (location.isPresent()) {
			dataCache.put(location.get(), Optional.of(data));
			
			keywords.forEach(keyword -> {
				try {
					keysCache.get(keyword, () -> new HashSet<>()).add(location.get());
				}
				catch (ExecutionException e) {
					throw new RuntimeException(e); // should never happen
				}
			});
		}
		
		return location;
	}
	
	@Override
	public Stream<String> search(String keyword) {
		try {
			return keysCache.get(keyword, () -> backend.search(keyword).collect(toSet())).parallelStream();
		}
		catch (ExecutionException ee) {
			throw new RuntimeException(ee);
		}
	}

	@Override
	public Optional<String> fetch(String location) {
		try {
			return dataCache.get(location, () -> backend.fetch(location));
		}
		catch (ExecutionException ee) {
			throw new RuntimeException(ee);
		}
	}
}
