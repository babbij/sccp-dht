package com.goodforgoodbusiness.engine.dht.impl.remote;

import java.rmi.Naming;
import java.time.Duration;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/** A simple lookup cache to avoid doing Naming.lookup on every call */
class RemoteDHTLookupCache {
	private Cache<String, RemoteDHTNode> cache = CacheBuilder
		.newBuilder()
		.expireAfterWrite(Duration.ofMinutes(5))
		.build()
	;
	
	public RemoteDHTNode lookup(String nodeUrl) throws ExecutionException {		
		return cache.get(nodeUrl, () -> {
			return (RemoteDHTNode)Naming.lookup(nodeUrl);
		});
	}
	
	public void invalidate(String nodeUrl) {
		cache.invalidate(nodeUrl);
	}
}
