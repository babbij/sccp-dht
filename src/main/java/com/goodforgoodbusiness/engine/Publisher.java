package com.goodforgoodbusiness.engine;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.engine.crypto.EncryptionException;
import com.goodforgoodbusiness.engine.warp.Warp;
import com.goodforgoodbusiness.engine.weft.Weft;
import com.goodforgoodbusiness.model.StorableContainer;
import com.goodforgoodbusiness.model.TriTuple;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Publisher unifies the operations of publishing to both weft and warp.
 */
@Singleton
public class Publisher {
	private static final Logger log = Logger.getLogger(Publisher.class);
	
	private final Warp warp;
	private final Weft weft;
	
	private final ShareManager keyManager;
	
	@Inject
	public Publisher(Warp warp, Weft weft, ShareManager keyManager) {
		this.warp = warp;
		this.weft = weft;
		
		this.keyManager = keyManager;
	}
	
	/**
	 * Publish a container to the weft (storage) and warp (indexing)
	 */
	public boolean publish(StorableContainer container) throws EncryptionException {
		log.debug("Publishing container: " + container.getId());
		
		// encrypt with secret key + publish to weft
		var publishResult = weft.publish(container);
		
		if (publishResult.isPresent()) {
			// pointer should be encrypted with _all_ the possible patterns + other attributes
			var attributes = Attributes.forPublish(keyManager.getCreatorKey(), container.getTriples().map(t -> TriTuple.from(t)));
			
			// patterns to publish are all possible triple combinations
			// create + publish a pointer for each generated pattern
			container.getTriples()
				.flatMap(t -> Patterns.forPublish(keyManager, TriTuple.from(t)))
				.forEach(pattern -> warp.publish(container.getId(), pattern, attributes, publishResult.get().getKey()))
			;
			
			
			return true;
		}
		
		return false; // could not publish
	}
}
