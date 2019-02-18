package com.goodforgoodbusiness.engine.weft;

import com.goodforgoodbusiness.engine.crypto.key.EncodeableSecretKey;
import com.goodforgoodbusiness.model.EncryptedContainer;

/**
 * Result of Weft publish.
 */
public final class WeftPublishResult {
	private final EncodeableSecretKey key;
	private final EncryptedContainer container;
	
	private final String publishedData;
	private final String publishedLocation;
	
	WeftPublishResult(EncodeableSecretKey key, EncryptedContainer container, String publishedLocation, String publishedData) {
		this.key = key;
		this.container = container;
		
		this.publishedLocation = publishedLocation;
		this.publishedData = publishedData;
	}

	public EncodeableSecretKey getKey() {
		return key;
	}
	
	public EncryptedContainer getContainer() {
		return container;
	}
	
	/**
	 * This will be the container's JSON, encrypted.
	 */
	public String getPublishedData() {
		return publishedData;
	}
	
	public String getPublishedLocation() {
		return publishedLocation;
	}
}
