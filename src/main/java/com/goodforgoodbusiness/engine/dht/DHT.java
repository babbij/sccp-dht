package com.goodforgoodbusiness.engine.dht;

import java.util.stream.Stream;

import com.goodforgoodbusiness.model.EncryptedClaim;

public interface DHT {
	public Stream<String> getPointers(String pattern) ;
	public void putPointer(String pattern, String data);
	
	public EncryptedClaim getClaim(String id);
	public void putClaim(EncryptedClaim encryptedClaim);
}
