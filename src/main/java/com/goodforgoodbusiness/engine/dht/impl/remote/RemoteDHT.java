package com.goodforgoodbusiness.engine.dht.impl.remote;

import static java.util.Collections.emptySet;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toSet;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.dht.RosterClient;
import com.goodforgoodbusiness.dht.RosterException;
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
public class RemoteDHT extends MongoDHT implements DHT {
	private static final Logger log = Logger.getLogger(RemoteDHT.class);
	
	public static class RemoteNodeWrapper implements DHTNode {
		private DHTNode node;
		
		public RemoteNodeWrapper(DHTNode node) {
			this.node = node;
		}
		
		@Override
		public void ping() {
			throw new RuntimeException("Don't call ping on this wrapper");
		}
		
		public Set<String> getPointers(String pattern) {
			try {
				return node.getPointers(pattern);
			}
			catch (RemoteException e) {
				log.warn("RemoteException from a node, soft fail (" + e.getMessage() + ")");
				return emptySet();
			}
		}
		
		public String getClaim(String id) {
			try {
				return node.getClaim(id);
			}
			catch (RemoteException e) {
				log.warn("RemoteException from a node, soft fail (" + e.getMessage() + ")");
				return null;
			}
		}
	}
	
	private class DHTNodeImpl extends UnicastRemoteObject implements DHTNode {
		protected DHTNodeImpl() throws RemoteException {
			super();
		}

		@Override
		public void ping() throws RemoteException {
			log.debug("Pong");
		}

		@Override
		public Set<String> getPointers(String pattern) throws RemoteException {
			log.info("Remote request for pointers for " + pattern);
			
			// defer to MongoDHT
			return RemoteDHT.super
				.getPointers(pattern)
				.map(dhtp -> dhtp.getData())
				.collect(toSet())
			;
		}

		@Override
		public String getClaim(String id) throws RemoteException {
			log.info("Remote request for claim " + id);
			
			// defer to MongoDHT
			return Optional
				.ofNullable(RemoteDHT.super.getClaim(id, MongoDHT.META))
				.map(JSON::encodeToString)
				.orElse(null)
			;
		}
	};
	
	private final RosterClient<DHTNode> client;

	@Inject
	public RemoteDHT(
		@Named("dhtstore.connectionUrl") String connectionUrl,
		@Named("dht.rosterUrl") String rosterUrl)  throws RosterException, RemoteException {
		
		super(connectionUrl);
		this.client = new RosterClient<DHTNode>(rosterUrl, DHTNode.class, new DHTNodeImpl());
	}
	
	@Override
	public Stream<DHTPointer> getPointers(String pattern) {
		log.debug("Get pointers: " + pattern);
		
		try {
			return client
				.getRegistrations()
				.flatMap(reg -> {
					try {
						return reg.getRemote()
							.getPointers(pattern)
							.stream()
							.map(data -> 
								new DHTPointer(data, new DHTPointerMeta(singletonMap("location", reg.getLocation())))
							)
						;
					}
					catch (RemoteException e) {
						// silent fail
						return Stream.<DHTPointer>empty();
					}
				})
			;
		}
		catch (RosterException e) {
			log.error("Error getting pointers", e);
			return Stream.empty();
		}
	}

	@Override
	public Optional<EncryptedClaim> getClaim(String id, DHTPointerMeta meta) {
		log.debug("Get claim: " + id);
		
		return meta.get("location")
			.flatMap(loc -> client.lookupByLocation(loc))
			.flatMap(reg -> {
				try {
					return Optional.ofNullable(reg.getRemote().getClaim(id));
				}
				catch (RemoteException e) {
					log.error("Could not fetch claim: " + e.getMessage());
					return Optional.empty();
				}
			})
			.map(s -> JSON.decode(s, EncryptedClaim.class))
		;
	}
}
