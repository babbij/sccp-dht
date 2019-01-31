package com.goodforgoodbusiness.engine.dht;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

import java.util.Map;
import java.util.Optional;

public class DHTPointerMeta {
	private final Map<String, String> meta;
	
	public DHTPointerMeta(Map<String, String> meta) {
		this.meta = unmodifiableMap(meta);
	}
	
	public DHTPointerMeta() {
		this(emptyMap());
	}
	
	public Optional<String> get(String key) {
		return Optional.ofNullable(meta.get(key));
	}
}
