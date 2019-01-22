package com.goodforgoodbusiness.dhtjava.crypto.abe.key;

import java.io.UnsupportedEncodingException;
import java.security.Key;

public abstract class ABEKey implements Key {
	private final byte [] encoded;
	
	protected ABEKey(String key) {
		try {
			this.encoded = key.getBytes("US-ASCII");
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException("US-ASCII not supported", e);
		}
	}
	
	protected ABEKey(byte [] encoded) {
		this.encoded = encoded;
	}
	
	@Override
	public String getFormat() {
		return "ABE";
	}
	
	@Override
	public String getAlgorithm() {
		return "ABE";
	}
	
	@Override
	public byte[] getEncoded() {
		return encoded;
	}
	
	@Override
	public String toString() {
		try {
			return new String(encoded, "US-ASCII");
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException("US-ASCII not supported", e);
		}
	}
}
