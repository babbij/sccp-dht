package com.goodforgoodbusiness.dhtjava.crypto.abe.key;

import java.security.PrivateKey;

public class ABEShareKey extends ABEKey implements PrivateKey {
	public ABEShareKey(String privateKey) {
		super(privateKey);
	}
	
	public ABEShareKey(byte [] privateKey) {
		super(privateKey);
	}
}
