package com.goodforgoodbusiness.engine;

import java.time.Duration;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.model.TriTuple;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/** 
 * This doesn't actually cache triples, but prevents the system going to the DHT too often
 * or nonsensically. 
 * 
 * If we just queried for (foo, ANY, ANY) a query of (foo, blah, ANY) will not yield any
 * further results that we could decrypt unless our list of available sharekeys has changed.
 * 
 * The intended behaviour is that if this fails, you only draw from the local store.
 */
@Singleton
public class Governer {
	private static final Logger log = Logger.getLogger(Governer.class);
	
	// may eventually want to store something as the cache value
	// but for the moment, only need to see if it's present at all
	private static final Object PRESENT = new Object();
	
	private Cache<TriTuple, Object> tracker;
	
	@Inject
	public Governer(@Named("dht.cache.enabled") boolean enabled, @Named("dht.cache.duration") String cacheDuration) {
		this(enabled, Duration.parse(cacheDuration));
	}
	
	public Governer(boolean enabled, Duration cacheDuration) {
		if (enabled) {
			this.tracker = CacheBuilder
				.newBuilder()
				.expireAfterWrite(cacheDuration)
				.build()
			;
		}
		else {
			this.tracker = null;
		}
	}
	
	public boolean allow(TriTuple tuple) {
		if (tracker != null) {
			// calculate 'wider' combinations that would have netted this triple
			
			var any = tuple
				.matchingCombinations()
				.filter(c -> (tracker.getIfPresent(c) != null))
				.findFirst()
			;
			
			if (any.isPresent()) {
				if (log.isDebugEnabled()) log.debug("Deny search for " + tuple + " as recent search for " + any.get());
				return false; // present
			}
			else {
				tracker.put(tuple, PRESENT);
				return true;
			}
		}
		else {
			return true;
		}
	}
	
	public void invalidate(TriTuple tuple) {
		if (tracker != null) {
			// for the moment, do full invalidation here.
			// become more nuanced w.r.t. received SharedAcceptRequests with time.
			tracker.invalidateAll();
		}
	}
}
