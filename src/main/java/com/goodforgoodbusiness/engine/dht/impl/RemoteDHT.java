package com.goodforgoodbusiness.engine.dht.impl;

import static java.util.stream.Collectors.toSet;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.dht.DHTClient;
import com.goodforgoodbusiness.dht.DHTNode;
import com.goodforgoodbusiness.engine.dht.DHT;
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
public class RemoteDHT extends MongoDHT implements DHT {
	private static final Logger log = Logger.getLogger(RemoteDHT.class);
	
	private class RemoteDHTNode implements DHTNode {
		@Override
		public void ping() throws RemoteException {
		}

		@Override
		public String getBindName() throws RemoteException {
			return registryName;
		}

		@Override
		public Set<String> getPointers(String pattern) throws RemoteException {
			log.info("Remote request for pointers for " + pattern);
			
			// defer to MongoDHT
			return RemoteDHT.super
				.getPointers(pattern)
				.collect(toSet())
			;
		}

		@Override
		public String getClaim(String id) throws RemoteException {
			log.info("Remote request for claim " + id);
			
			// defer to MongoDHT
			return Optional
				.ofNullable(RemoteDHT.super.getClaim(id))
				.map(JSON::encodeToString)
				.orElse(null)
			;
		}
	}
	
	private final DHTClient dhtClient;
	private final DHTNode node;
	private final Remote nodeStub;
	private final String registryName;

	@Inject
	public RemoteDHT(
		@Named("dht.store.connectionUrl") String connectionUrl,
		@Named("dht.registry.name") String registryName,
		@Named("dht.registry.host") String registryHost,
		@Named("dht.registry.port") int registryPort) throws RemoteException {
		
		super(connectionUrl);
		
		this.dhtClient = new DHTClient(registryName, registryHost, registryPort);
		this.registryName = registryName;
		
		this.node = new RemoteDHTNode();
		this.nodeStub = UnicastRemoteObject.exportObject(node, 0);
		
		var registry = LocateRegistry.getRegistry(registryHost, registryPort);
		registry.rebind(node.getBindName(), nodeStub);
		
		var hypervisor = new Thread() {
			public void run() {
				try {
					registry.rebind(node.getBindName(), nodeStub);
				}
				catch (Exception e) {
				}
			}
		};
		
		hypervisor.setDaemon(true);
		hypervisor.start();
	}
	
	@Override
	public Stream<String> getPointers(String pattern) {
		log.debug("Get pointers: " + pattern);
	
		return dhtClient
			.getPointers(pattern)
			.values()
			.stream()
			.flatMap(Set::stream)
			.filter(s -> s != null)
		;
	}

	@Override
	public EncryptedClaim getClaim(String id) {
		log.debug("Get claim: " + id);
		
		return dhtClient
			.getClaims(id)
			.values()
			.stream()
			.filter(s -> s != null)
			.findFirst()
			.map(s -> JSON.decode(s, EncryptedClaim.class))
			.orElse(null)
		;
	}
}
