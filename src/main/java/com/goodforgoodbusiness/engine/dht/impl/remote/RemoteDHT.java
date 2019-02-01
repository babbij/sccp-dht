package com.goodforgoodbusiness.engine.dht.impl.remote;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.engine.dht.DHT;
import com.goodforgoodbusiness.engine.dht.DHTPointer;
import com.goodforgoodbusiness.engine.dht.DHTPointerMeta;
import com.goodforgoodbusiness.engine.dht.impl.MongoDHT;
import com.goodforgoodbusiness.model.EncryptedClaim;
import com.goodforgoodbusiness.shared.encode.JSON;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Store claims on our pseudo-DHT for testing.
 * 
 * Extends MongoDHT because it stores claims there for retrieval,
 * but this time by the same instance and others over RMI.
 */
@Singleton
public class RemoteDHT implements DHT {
	private static final Logger log = Logger.getLogger(RemoteDHT.class);
	
	private final RemoteDHTLookupCache nodeLookup = new RemoteDHTLookupCache();
	
	private final MongoDHT mongo;
	private final List<String> nodeUrls;
	
	@Inject
	public RemoteDHT(
		@Named("dhtstore.connectionUrl") String connectionUrl,
		@Named("dht.port") int port,
		@Named("dht.nodes") String nodeList) throws RemoteException, AlreadyBoundException {
		
		this.mongo = new MongoDHT(connectionUrl);
		this.nodeUrls = 
			asList(nodeList.trim().split(","))
				.stream()
				.map(String::trim)
				.filter(s -> s.length() > 0)
				.collect(toList())
		;
		
		// create a registry for self and bind in to it
		var registry = LocateRegistry.createRegistry(port);
		registry.bind("node", new RemoteDHTNodeImpl(mongo));
	}
	
	@Override
	public void putPointer(String pattern, String data) {
		mongo.putPointer(pattern, data);
	}
	
	@Override
	public Stream<DHTPointer> getPointers(String pattern) {
		log.debug("Get pointers: " + pattern);
		
		var results = nodeUrls
			.parallelStream()
			.map(nodeUrl -> getPointers(pattern, nodeUrl))
		;
		
		return results.flatMap(identity());
	}
	
	private Stream<DHTPointer> getPointers(String pattern, String nodeUrl) {
		log.debug("Trying node: " + nodeUrl);
		
		// run this twice in case of net problems
		for (var i = 0; i < 2; i++) {
			try {
				var node = nodeLookup.lookup(nodeUrl);
				return
					node.getPointers(pattern)
						.stream()
						.map(data -> new DHTPointer(data, new DHTPointerMeta(singletonMap("node", nodeUrl))))
				;
			}
			catch (RemoteException | ExecutionException e) {
				log.error("Could not fetch claim: " + e.getMessage());
				nodeLookup.invalidate(nodeUrl);
				
				try {
					Thread.sleep(100);
				}
				catch (InterruptedException ee) {
					// continue
				}
			}
		}
		
		return Stream.empty();
	}

	@Override
	public void putClaim(EncryptedClaim claim) {
		mongo.putClaim(claim);
	}
	
	@Override
	public Optional<EncryptedClaim> getClaim(String id, DHTPointerMeta meta) {
		log.debug("Get claim: " + id);
		return meta.get("node").flatMap(url -> getClaim(id, url));
	}
	
	private Optional<EncryptedClaim> getClaim(String id, String nodeUrl) {
		// run this twice in case of net problems
		for (var i = 0; i < 2; i++) {
			try {
				var node = nodeLookup.lookup(nodeUrl);
				return Optional
					.ofNullable(node.getClaim(id))
					.map(result -> JSON.decode(result, EncryptedClaim.class))
				;
			}
			catch (RemoteException | ExecutionException e) {
				log.error("Could not fetch claim: " + e.getMessage());
				nodeLookup.invalidate(nodeUrl);
				
				try {
					Thread.sleep(100);
				}
				catch (InterruptedException ee) {
					// continue
				}
			}
		}
		
		return Optional.empty();
	}
}
