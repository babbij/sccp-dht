package com.goodforgoodbusiness.dhtjava.keys;

import java.security.KeyPair;

import com.goodforgoodbusiness.dhtjava.crypto.abe.key.ABEPublicKey;
import com.goodforgoodbusiness.dhtjava.crypto.abe.key.ABEShareKey;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StoredKey {
	@Expose
	@SerializedName("public")
	private String publicKey;
	
	@Expose
	@SerializedName("share")
	private String shareKey;

	public StoredKey(KeyPair pair) {
		this.publicKey = pair.getPublic().toString();
		this.shareKey = pair.getPrivate().toString();
	}
	
	@Override
	public String toString() {
		return "StoredKey(" + publicKey + ", " + shareKey + ")";
	}

	public KeyPair toKeyPair() {
		return new KeyPair(new ABEPublicKey(publicKey), new ABEShareKey(shareKey));
	}
}
