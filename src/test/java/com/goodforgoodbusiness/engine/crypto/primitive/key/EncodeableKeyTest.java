package com.goodforgoodbusiness.engine.crypto.primitive.key;

import com.goodforgoodbusiness.engine.crypto.primitive.SymmetricEncryption;
import com.goodforgoodbusiness.engine.crypto.primitive.key.EncodeableSecretKey;
import com.goodforgoodbusiness.shared.encode.JSON;

public class EncodeableKeyTest {
	public static void main(String[] args) {
		var key = SymmetricEncryption.createKey();
		var json = JSON.encodeToString(key);
		
		System.out.println(json);
		
		JSON.decode(json, EncodeableSecretKey.class);
	}
}
