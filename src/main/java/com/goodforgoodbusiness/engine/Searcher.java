package com.goodforgoodbusiness.engine;

import static java.util.stream.Stream.empty;

import java.util.HashSet;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.engine.crypto.EncryptionException;
import com.goodforgoodbusiness.engine.crypto.key.EncodeableSecretKey;
import com.goodforgoodbusiness.engine.store.container.ContainerStore;
import com.goodforgoodbusiness.engine.warp.Warp;
import com.goodforgoodbusiness.engine.weft.Weft;
import com.goodforgoodbusiness.model.StorableContainer;
import com.goodforgoodbusiness.model.TriTuple;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Searcher brings together facilities from the Weft and Warp to find containers
 */
@Singleton
public class Searcher {
	private static final Logger log = Logger.getLogger(Searcher.class);
	
	private final Warp warp;
	private final Weft weft;
	
	private final Governer governor;
	private final ContainerStore store;
	
	@Inject
	public Searcher(Warp warp, Weft weft, Governer governor, ContainerStore store) {
		this.store = store;
		this.warp = warp;
		this.weft = weft;
		this.governor = governor;
	}
	
	/**
	 * Search the warp + weft for a triple pattern
	 */
	public Stream<StorableContainer> search(TriTuple tuple, boolean saveToLocal) {
		if (governor.allow(tuple)) {
			log.info("DHT searching for " + tuple);

			// process the stream because these operations have side effects.
			var containers = new HashSet<StorableContainer>();
			
			warp.search(tuple)
				// only work on containers we've not already seen/stored.
				.filter(pointer -> !store.contains(pointer.getContainerId()))
				.flatMap(pointer -> {
					try {
						// attempt fetch & decrypt
						return weft.fetch(
							pointer.getContainerId(),
							new EncodeableSecretKey(pointer.getContainerKey())
						).stream(); // stream so it's just non-empty Optionals
					}
					catch (EncryptionException e) {
						log.error("Couldn not decrypt container", e);
						return Stream.empty();
					}
				})
				.forEach(container -> {
					if (saveToLocal) {
						store.save(container);
					}
					
					containers.add(container);
				});
			
			log.debug("Results found = " + containers.size());
			return containers.parallelStream();
		}
		else {
			if (log.isDebugEnabled()) log.debug("DHT governer deny on " + tuple + " (recently accessed)");
			return empty();
		}
	}
}
