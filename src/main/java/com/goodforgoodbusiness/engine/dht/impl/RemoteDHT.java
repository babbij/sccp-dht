package com.goodforgoodbusiness.engine.dht.impl;

import static java.util.Collections.emptySet;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

@Singleton
public class RemoteDHT implements DHT {
	private static final Logger log = Logger.getLogger(RemoteDHT.class);
	
	private Map<String, Set<String>> pointers = new HashMap<>();
	private Map<String, String> claims = new HashMap<>();
	
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
			return pointers.getOrDefault(pattern, emptySet());
		}

		@Override
		public String getClaim(String id) throws RemoteException {
			log.info("Remote request for claim " + id);
			return claims.get(id);
		}
	}
	
	private final DHTClient client;
	private final DHTNode node;
	private final Remote nodeStub;
	private final String registryName;

	@Inject
	public RemoteDHT(
		@Named("dht.registry.name") String registryName,
		@Named("dht.registry.host") String registryHost,
		@Named("dht.registry.port") int registryPort) throws RemoteException {
		
		this.client = new DHTClient(registryName, registryHost, registryPort);
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
	
		return client
			.getPointers(pattern)
			.values()
			.stream()
			.flatMap(Set::stream)
			.filter(s -> s != null)
		;
	}

	@Override
	public void putPointer(String pattern, String data) {
		log.debug("Put pointer: " + pattern);
		
		var existing = pointers.get(pattern);
		if (existing != null) {
			existing.add(data);
		}
		else {
			var newSet = new HashSet<String>();
			newSet.add(data);
			pointers.put(pattern, newSet);
		}
	}
	
	@Override
	public EncryptedClaim getClaim(String id) {
		log.debug("Get claim: " + id);
		
		return client
			.getClaims(id)
			.values()
			.stream()
			.filter(s -> s != null)
			.findFirst()
			.map(s -> JSON.decode(s, EncryptedClaim.class))
			.orElse(null)
		;
	}

	@Override
	public void putClaim(EncryptedClaim claim) {
		log.debug("Put claim: " + claim.getId());
		claims.put(claim.getId(), JSON.encodeToString(claim));
	}

}
