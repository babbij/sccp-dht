package com.goodforgoodbusiness.engine.dht.impl.remote;

import static java.util.Arrays.asList;
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
import com.goodforgoodbusiness.engine.dht.impl.MongoDHT;
import com.goodforgoodbusiness.model.EncryptedClaim;
import com.goodforgoodbusiness.model.EncryptedPointer;
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
	public void putPointer(String pattern, EncryptedPointer data) {
		mongo.putPointer(pattern, data);
	}
	
	@Override
	public Stream<EncryptedPointer> getPointers(String pattern) {
		log.debug("Get pointers: " + pattern);
		
		return nodeUrls
			.parallelStream()
			.flatMap(nodeUrl -> 
				getPointers(pattern, nodeUrl).map(data -> new RetrievedPointer(data, nodeUrl))
			)
		;
	}
	
	private Stream<String> getPointers(String pattern, String nodeUrl) {
		log.debug("Trying node: " + nodeUrl);
		
		// run this twice in case of net problems
		for (var i = 0; i < 2; i++) {
			try {
				return nodeLookup.lookup(nodeUrl).getPointers(pattern).stream();
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
	public Optional<EncryptedClaim> getClaim(String id, EncryptedPointer pointer) {
		if (!(pointer instanceof RetrievedPointer)) {
			throw new IllegalArgumentException("Can only get claims for pointers retrieved directly from the DHT"); 
		}
		
		String nodeUrl = ((RetrievedPointer)pointer).nodeUrl;
		log.debug("Get claim: " + id + " from " + nodeUrl);
		
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
