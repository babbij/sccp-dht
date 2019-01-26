package com.goodforgoodbusiness.engine.dht;

import java.time.Duration;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.engine.Pattern;
import com.goodforgoodbusiness.engine.store.keys.spec.ShareKeySpec;
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
public class DHTAccessGovernor {
	// may eventually want to store something as the cache value
	// but for the moment, only need to see if it's present at all
	private static final Object PRESENT = new Object();
	
	private Cache<Triple, Object> tracker;
	
	@Inject
	public DHTAccessGovernor(@Named("dht.cache.enabled") boolean enabled, @Named("dht.cache.duration") String cacheDuration) {
		this(enabled, Duration.parse(cacheDuration).getSeconds());
	}
	
	public DHTAccessGovernor(boolean enabled, long cacheDurationSeconds) {
		if (enabled) {
			this.tracker = CacheBuilder
				.newBuilder()
				.expireAfterWrite(Duration.ofSeconds(cacheDurationSeconds))
				.build()
			;
		}
		else {
			this.tracker = null;
		}
	}
	
	public boolean allow(Triple triple) {
		if (tracker != null) {
			// calculate 'wider' combinations that would have netted this triple
			var any = Pattern
				.combinations(triple)
				.stream()
				.filter(c -> (tracker.getIfPresent(c) != null))
				.findFirst()
				.map(c -> true)
				.orElse(false)
			;
			
			if (any) {
				return false; // present
			}
			else {
				tracker.put(triple, PRESENT);
				return true;
			}
		}
		else {
			return true;
		}
	}
	
	public void invalidate(ShareKeySpec spec) {
		if (tracker != null) {
			// for the moment, do full invalidation here.
			// become more nuanced w.r.t. received SharedAcceptRequests with time.
			tracker.invalidateAll();
		}
	}
}
