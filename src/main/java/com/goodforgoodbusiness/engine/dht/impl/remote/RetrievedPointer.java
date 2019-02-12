package com.goodforgoodbusiness.engine.dht.impl.remote;

import com.goodforgoodbusiness.model.EncryptedPointer;

class RetrievedPointer extends EncryptedPointer {
	protected final String nodeUrl;
	
	protected RetrievedPointer(String data, String nodeUrl) {
		super(data);
		this.nodeUrl = nodeUrl;
	}
}
