package com.goodforgoodbusiness.engine.dht;

/**
 * Wrapper that combines the actual pointer with some metadata
 * that lets us easily remember things (like what node might hold a claim).
 */
public class DHTPointer {
	private final String data;
	private final DHTPointerMeta meta;
	
	public DHTPointer(String data, DHTPointerMeta meta) {
		this.data = data;
		this.meta = meta;
	}
	
	public String getData() {
		return data;
	}

	public DHTPointerMeta getMeta() {
		return meta;
	}
}
