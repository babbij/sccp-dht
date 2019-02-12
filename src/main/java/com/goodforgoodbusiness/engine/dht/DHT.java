package com.goodforgoodbusiness.engine.dht;

import java.util.Optional;
import java.util.stream.Stream;

import com.goodforgoodbusiness.model.EncryptedClaim;
import com.goodforgoodbusiness.model.EncryptedPointer;

public interface DHT {
	public Stream<EncryptedPointer> getPointers(String pattern) ;
	public void putPointer(String pattern, EncryptedPointer pointer);
	
	public Optional<EncryptedClaim> getClaim(String id, EncryptedPointer originalPointer);
	public void putClaim(EncryptedClaim encryptedClaim);
}
