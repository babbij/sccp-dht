package com.goodforgoodbusiness.dhtjava.crypto.abe.key;

import java.security.PublicKey;

public class ABEPublicKey extends ABEKey implements PublicKey {
	public ABEPublicKey(String publicKey) {
		super(publicKey);
	}
	
	public ABEPublicKey(byte [] publicKey) {
		super(publicKey);
	}
}
