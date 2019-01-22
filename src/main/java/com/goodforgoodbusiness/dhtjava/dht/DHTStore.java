package com.goodforgoodbusiness.dhtjava.dht;

import java.util.stream.Stream;

import com.goodforgoodbusiness.shared.model.EncryptedClaim;

public interface DHTStore {
	public Stream<String> getPointers(String pointer) ;
	public void putPointer(String pattern, String data);
	
	public EncryptedClaim getClaim(String id);
	public void putClaim(EncryptedClaim encryptedClaim);
}
