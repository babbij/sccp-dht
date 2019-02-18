package com.goodforgoodbusiness.engine.backend.impl.remote;

import static java.util.stream.Collectors.toSet;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.engine.backend.impl.MongoDHTBackend;

public class RemoteDHTNodeImpl extends UnicastRemoteObject implements RemoteDHTNode {
	private static final Logger log = Logger.getLogger(RemoteDHTNodeImpl.class);
	
	private final MongoDHTBackend backend;
	
	protected RemoteDHTNodeImpl(MongoDHTBackend backend) throws RemoteException {
		super();
		this.backend = backend;
	}
	
	@Override
	public String fetch(String location) throws RemoteException {
		log.debug("Remote fetch request for " + location);
		var result = backend.fetch(location).orElse(null);
		log.debug("Remote fetch returning result: " + (result != null));
		return result;
	}
	
	@Override
	public Set<String> search(String keyword) throws RemoteException {
		log.debug("Remote search request for " + keyword);
		var results = backend.search(keyword).collect(toSet());
		log.debug("Remote search returning " + results.size() + " results");
		return results;
	}
}
