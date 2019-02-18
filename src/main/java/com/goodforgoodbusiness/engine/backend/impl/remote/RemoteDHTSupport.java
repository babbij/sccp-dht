package com.goodforgoodbusiness.engine.backend.impl.remote;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class RemoteDHTSupport {
	private Cache<String, Remote> lookupCache = CacheBuilder
		.newBuilder()
		.expireAfterWrite(Duration.ofMinutes(10))
		.build()
	;
	
	public class RMINode {
		private final String url;
		
		public RMINode(String url) {
			this.url = url;
		}
		
		public String getUrl() {
			return url;
		}
		
		public Optional<RemoteDHTNode> lookup() {
			try {
				
				return Optional.of(
					(RemoteDHTNode)lookupCache.get(url, () -> {
						return Naming.lookup(url);
					})
				);
			}
			catch (ExecutionException ee) {
				return Optional.empty();
			}
		}

		public void invalidate() {
			lookupCache.invalidate(url);
		}
	}
	
	private final Registry registry;
	private final List<String> nodeUrls;
	
	@Inject
	public RemoteDHTSupport(@Named("dht.port") int port, @Named("dht.nodes") String nodeList) throws RemoteException {
		this.nodeUrls = 
			asList(nodeList.trim().split(","))
				.stream()
				.map(String::trim)
				.filter(s -> s.length() > 0)
				.collect(toList())
		;
		
		// create a registry for self and bind in to it
		this.registry = LocateRegistry.createRegistry(port);		
	}
	
	public void bind(RemoteDHTNode node) throws AccessException, RemoteException, AlreadyBoundException {
		registry.bind("node", node);
	}

	public RMINode node(String url) {
		return new RMINode(url);
	}
	
	public Stream<RMINode> nodes() {
		return nodeUrls.parallelStream().map(RMINode::new);
	}
}
