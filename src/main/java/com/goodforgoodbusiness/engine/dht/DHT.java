package com.goodforgoodbusiness.engine.dht;

import java.util.Optional;
import java.util.stream.Stream;

import com.goodforgoodbusiness.model.EncryptedClaim;

public interface DHT {
	public Stream<DHTPointer> getPointers(String pattern) ;
	public void putPointer(String pattern, String data);
	
	public Optional<EncryptedClaim> getClaim(String id, DHTPointerMeta meta);
	public void putClaim(EncryptedClaim encryptedClaim);
}
