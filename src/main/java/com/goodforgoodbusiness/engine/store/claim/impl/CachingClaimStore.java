package com.goodforgoodbusiness.engine.store.claim.impl;

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

import com.goodforgoodbusiness.engine.store.claim.ClaimStore;
import com.goodforgoodbusiness.model.StoredClaim;
import com.goodforgoodbusiness.model.TriTuple;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;


/**
 * Caching wrapper for ClaimStore.
 */
@Singleton
public class CachingClaimStore implements ClaimStore {
	@BindingAnnotation @Target({ FIELD, PARAMETER, METHOD }) @Retention(RUNTIME)
	public @interface Underlying {}
	
	private final Cache<String, Optional<StoredClaim>> claimsById;
	private final Cache<TriTuple, Set<StoredClaim>> claimsByPattern;
	
	private final ClaimStore underlying;
	
	@Inject
	public CachingClaimStore(@Underlying ClaimStore underlying, @Named("claimstore.cache.duration") String cacheDuration) {
		this(underlying, Duration.parse(cacheDuration));
	}
	
	public CachingClaimStore(ClaimStore underlying, Duration cacheDuration) {
		this.underlying = underlying;
		
		this.claimsById = CacheBuilder
			.newBuilder()
			.expireAfterWrite(cacheDuration)
			.maximumSize(1000)
			.build()
		;
		
		this.claimsByPattern = CacheBuilder
			.newBuilder()
			.expireAfterWrite(cacheDuration)
			.maximumSize(5000)
			.build()
		;
	}

	@Override
	public void save(StoredClaim claim) {
		underlying.save(claim);
		
		claim.getTriples()
			.flatMap(triple -> TriTuple.from(triple).matchingCombinations())
			.forEach(pattern -> claimsByPattern.invalidate(pattern));
		;
		
		claimsById.put(claim.getId(), Optional.of(claim));
	}

	@Override
	public Stream<StoredClaim> search(TriTuple tt) {
		try {
			Set<StoredClaim> set = claimsByPattern.get(tt, () -> underlying.search(tt).collect(Collectors.toSet()));
			set.forEach(claim -> claimsById.put(claim.getId(), Optional.of(claim)));
			return set.parallelStream();
		}
		catch (ExecutionException ee) {
			throw new RuntimeException(ee);
		}
	}
	
	@Override
	public Optional<StoredClaim> getClaim(String id) {
		try {
			return claimsById.get(id, () -> underlying.getClaim(id));
		}
		catch (ExecutionException ee) {
			throw new RuntimeException(ee);
		}
	}
	
	@Override
	public boolean contains(String claimId) {
		return getClaim(claimId).isPresent();
	}
}
