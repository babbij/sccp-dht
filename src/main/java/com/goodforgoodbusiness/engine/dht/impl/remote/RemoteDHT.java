package com.goodforgoodbusiness.engine.dht.impl.remote;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.List;
import java.util.Optional;
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
	
	private final MongoDHT mongo;
	private final List<String> nodeUrls;
	
	@Inject
	public RemoteDHT(
		@Named("dhtstore.connectionUrl") String connectionUrl,
		@Named("dht.port") int port,
		@Named("dht.nodes") String nodeList) throws RemoteException, AlreadyBoundException {
		
		this.mongo = new MongoDHT(connectionUrl);
		this.nodeUrls = 
			asList(nodeList.split(","))
				.stream()
				.map(String::trim)
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
		
		var result = Stream.<DHTPointer>empty();
		
		for (String nodeUrl : nodeUrls) {
			try {
				var node = (RemoteDHTNode)Naming.lookup(nodeUrl);
				result = concat(
					node.getPointers(pattern)
						.stream()
						.map(data -> new DHTPointer(data, new DHTPointerMeta(singletonMap("node", nodeUrl)))),
					result
				);
			}
			catch (RemoteException | MalformedURLException | NotBoundException e) {
				log.error("Could not fetch claim: " + e.getMessage());
				// skip - take no action
			}
		}
		
		return result;
	}

	@Override
	public void putClaim(EncryptedClaim claim) {
		mongo.putClaim(claim);
	}
	
	@Override
	public Optional<EncryptedClaim> getClaim(String id, DHTPointerMeta meta) {
		log.debug("Get claim: " + id);
		
		try {
			var nodeURL = meta.get("node");
			if (nodeURL.isPresent()) {
				var node = (RemoteDHTNode)Naming.lookup(nodeURL.get());
				return Optional
					.ofNullable(node.getClaim(id))
					.map(result -> JSON.decode(result, EncryptedClaim.class))
				;
			}
			else {
				return Optional.empty();
			}
		}
		catch (RemoteException | MalformedURLException | NotBoundException e) {
			log.error("Could not fetch claim: " + e.getMessage());
			return Optional.empty();
		}
	}
}
