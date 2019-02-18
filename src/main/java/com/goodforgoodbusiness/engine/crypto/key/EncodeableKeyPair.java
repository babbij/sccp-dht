package com.goodforgoodbusiness.engine.crypto.key;

import java.security.KeyPair;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public final class EncodeableKeyPair {
	@Expose
	@SerializedName("private")
	private EncodeablePrivateKey privateKey;
	
	@Expose
	@SerializedName("public")
	private EncodeablePublicKey publicKey;
	
	public EncodeableKeyPair(EncodeablePublicKey publicKey, EncodeablePrivateKey privateKey) {
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}
	
	public EncodeablePublicKey getPublic() {
		return publicKey;
	}

	public EncodeablePrivateKey getPrivate() {
		return privateKey;
	}
	
	@Override
	public int hashCode() {
		return privateKey.hashCode() ^ publicKey.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		
		if (o instanceof EncodeableKeyPair) {
			return
				publicKey.equals(((EncodeableKeyPair)o).publicKey) &&
				privateKey.equals(((EncodeableKeyPair)o).privateKey)
			;
		}
		
		if (o instanceof KeyPair) {
			return
				publicKey.equals(((KeyPair)o).getPublic()) &&
				privateKey.equals(((KeyPair)o).getPrivate())
			;
		}
		
		return false;
	}
}
