package com.goodforgoodbusiness.dhtjava.crypto.abe.key;

import javax.crypto.SecretKey;

public class ABESecretKey extends ABEKey implements SecretKey {
	public ABESecretKey(String publicKey) {
		super(publicKey);
	}
	
	public ABESecretKey(byte [] publicKey) {
		super(publicKey);
	}
}
