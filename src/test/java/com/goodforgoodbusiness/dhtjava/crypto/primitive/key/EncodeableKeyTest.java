package com.goodforgoodbusiness.dhtjava.crypto.primitive.key;

import com.goodforgoodbusiness.dhtjava.crypto.primitive.SymmetricEncryption;
import com.goodforgoodbusiness.shared.encode.JSON;

public class EncodeableKeyTest {
	public static void main(String[] args) {
		var key = SymmetricEncryption.createKey();
		var json = JSON.encodeToString(key);
		
		System.out.println(json);
		
		JSON.decode(json, EncodeableSecretKey.class);
	}
}
