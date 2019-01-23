package com.goodforgoodbusiness.dhtjava.crypto.primitive.key;

import com.goodforgoodbusiness.kpabe.KPABEInstance;
import com.goodforgoodbusiness.shared.JSON;

public class EncodeableShareKeyTest {
	public static void main(String[] args) throws Exception {
		var instance = KPABEInstance.newKeys();
		
		var keyPair = instance.shareKey("foo");
		var encodeablePair = new EncodeableShareKey(keyPair);
		var json = JSON.encodeToString(encodeablePair);
		
		System.out.println(json);
		
		EncodeableShareKey keyPair2 = JSON.decode(json, EncodeableShareKey.class);
		
		System.out.println(keyPair2.getPublic().toString());
		System.out.println(keyPair2.getShare().toString());
	}
}
