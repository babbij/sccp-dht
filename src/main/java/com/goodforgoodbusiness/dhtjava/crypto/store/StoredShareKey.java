package com.goodforgoodbusiness.dhtjava.dht.share;

import java.security.KeyPair;

import com.goodforgoodbusiness.kpabe.key.KPABEPublicKey;
import com.goodforgoodbusiness.kpabe.key.KPABEShareKey;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StoredShareKey {
	@Expose
	@SerializedName("public")
	private String publicKey;
	
	@Expose
	@SerializedName("share")
	private String shareKey;

	public StoredShareKey(KeyPair pair) {
		this.publicKey = pair.getPublic().toString();
		this.shareKey = pair.getPrivate().toString();
	}
	
	@Override
	public String toString() {
		return "StoredKey(" + publicKey + ", " + shareKey + ")";
	}

	public KeyPair toKeyPair() {
		return new KeyPair(new KPABEPublicKey(publicKey), new KPABEShareKey(shareKey));
	}
}
