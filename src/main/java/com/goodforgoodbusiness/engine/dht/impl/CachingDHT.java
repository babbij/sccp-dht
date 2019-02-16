package com.goodforgoodbusiness.engine.dht.impl;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.stream.Collectors.toSet;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import com.goodforgoodbusiness.engine.dht.DHT;
import com.goodforgoodbusiness.model.EncryptedContainer;
import com.goodforgoodbusiness.model.EncryptedPointer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * A cache layer for retrieving containers in the collection representing what's loaded on to the DHT
 */
@Singleton
public class CachingDHT implements DHT {
	@BindingAnnotation @Target({ FIELD, PARAMETER, METHOD }) @Retention(RUNTIME)
	public @interface Underlying {}
	
	private final Cache<String, Set<EncryptedPointer>> pointerCache;
	private final Cache<String, Optional<EncryptedContainer>> containerCache;
	private final DHT underlying;
	
	@Inject
	public CachingDHT(@Underlying DHT underlying, Duration cacheDuration) {
		this.pointerCache = CacheBuilder
			.newBuilder()
			.expireAfterWrite(cacheDuration)
			.maximumSize(5000)
			.build()
		;
		
		this.containerCache = CacheBuilder
			.newBuilder()
			.expireAfterWrite(cacheDuration)
			.maximumSize(1000)
			.build()
		;
		
		this.underlying = underlying;
	}
	
	@Override
	public Stream<EncryptedPointer> getPointers(String pattern) {
		try {
			return pointerCache.get(pattern, () -> underlying.getPointers(pattern).collect(toSet())).parallelStream();
		}
		catch (ExecutionException ee) {
			throw new RuntimeException(ee);
		}
	}

	@Override
	public void putPointer(String pattern, EncryptedPointer pointer) {
		underlying.putPointer(pattern, pointer);
		pointerCache.invalidate(pattern);
	}

	@Override
	public Optional<EncryptedContainer> getContainer(String id, EncryptedPointer originalPointer) {
		try {
			return containerCache.get(id, () -> underlying.getContainer(id, originalPointer));
		}
		catch (ExecutionException ee) {
			throw new RuntimeException(ee);
		}
	}

	@Override
	public void putContainer(EncryptedContainer encryptedContainer) {
		underlying.putContainer(encryptedContainer);
		containerCache.put(encryptedContainer.getId(), Optional.of(encryptedContainer));
	}
}
