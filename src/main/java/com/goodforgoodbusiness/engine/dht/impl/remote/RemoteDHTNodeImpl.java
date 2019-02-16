package com.goodforgoodbusiness.engine.dht.impl.remote;

import static java.util.stream.Collectors.toSet;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.engine.dht.impl.MongoDHT;
import com.goodforgoodbusiness.model.EncryptedPointer;
import com.goodforgoodbusiness.shared.encode.JSON;

public class RemoteDHTNodeImpl extends UnicastRemoteObject implements RemoteDHTNode {
	private static final Logger log = Logger.getLogger(RemoteDHTNodeImpl.class);
	
	private final MongoDHT store;
	
	protected RemoteDHTNodeImpl(MongoDHT store) throws RemoteException {
		super();
		this.store = store;
	}

	@Override
	public Set<String> getPointers(String pattern) throws RemoteException {
		log.debug("Remote request for pointers for " + pattern);
			
		// defer to MongoDHT
		return store
			.getPointers(pattern)
			.map(EncryptedPointer::getData)
			.collect(toSet())
		;
	}

	@Override
	public String getContainer(String id) throws RemoteException {
		log.debug("Remote request for container " + id);
			
		// defer to MongoDHT
		return store
			.getContainer(id) 
			.map(JSON::encodeToString)
			.orElse(null)
		;
	}
}
