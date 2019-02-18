package com.goodforgoodbusiness.engine.backend.impl.remote;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.engine.backend.DHTBackend;
import com.goodforgoodbusiness.engine.backend.impl.MongoDHTBackend;
import com.goodforgoodbusiness.engine.backend.impl.remote.RemoteDHTSupport.RMINode;
import com.goodforgoodbusiness.shared.Retry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Store containers on our pseudo-DHT for testing.
 * 
 * Extends MongoDHT because it stores containers there for retrieval,
 * but this time by the same instance and others over RMI.
 */
@Singleton
public class RemoteDHTBackend implements DHTBackend {
	private static final Logger log = Logger.getLogger(RemoteDHTBackend.class);
	
	private final RemoteDHTSupport rmi;
	private final MongoDHTBackend backend;  // stores what's been made available on this node
	
	@Inject
	public RemoteDHTBackend(@Named("dhtstore.connectionUrl") String connectionUrl, RemoteDHTSupport rmi) 
		throws RemoteException, AlreadyBoundException {
		
		this.backend = new MongoDHTBackend(connectionUrl);
		
		this.rmi = rmi;
		this.rmi.bind(new RemoteDHTNodeImpl(backend));
	}

	@Override
	public String publish(Set<String> keywords, String data) {
		return backend.publish(keywords, data); // write-through to Mongo.
	}
	
	@Override
	public Stream<String> search(String keyword) {
		log.debug("Search for " + keyword);
		return rmi.nodes().flatMap(node -> search(node, keyword));
	}
	
	private Stream<String> search(RMINode node, String keyword) {
		log.debug("Trying node: " + node.getUrl());
		
		try {
			return Retry.twice(() ->
				node.lookup().orElseThrow().search(keyword).stream().map(location -> node.getUrl() + "/" + location)
			);
		}
		catch (ExecutionException e) {
			log.error("Failed on " + node.getUrl() + ": " + e.getMessage());
			node.invalidate();
			return Stream.empty();
		}
	}
	
	@Override
	public Optional<String> fetch(String location) {
		log.debug("Fetch of " + location);
		
		// split back in to node + id
		var url = location.substring(0, location.lastIndexOf('/'));
		var remoteLocation = location.substring(location.lastIndexOf('/') + 1);
		
		var node = rmi.node(url);
		
		try {
			return Retry.twice(() ->
				Optional.ofNullable(node.lookup().orElseThrow().fetch(remoteLocation))
			);
		}
		catch (ExecutionException e) {
			log.error("Failed on " + node.getUrl() + ": " + e.getMessage());
			node.invalidate();
			return Optional.empty();	
		}
	}
}
