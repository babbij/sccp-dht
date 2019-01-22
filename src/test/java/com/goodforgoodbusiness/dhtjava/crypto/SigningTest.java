package com.goodforgoodbusiness.dhtjava.crypto;

import com.goodforgoodbusiness.dhtjava.crypto.KeyEncoder;
import com.goodforgoodbusiness.dhtjava.crypto.Signing;

public class SigningTest {
	public static void main(String[] args) throws Exception {
		var keys = Signing.generateKeyPair();
		
		System.out.println(keys.getPrivate().getClass());
		
		var encPrivKey = KeyEncoder.encodeKey(keys.getPrivate());
		System.out.println(encPrivKey);
		
		var privKey = KeyEncoder.decodePrivateKey(encPrivKey);
		
		var signature = Signing.sign("hello world", privKey);
		System.out.println(signature);
		
		var encPubKey = KeyEncoder.encodeKey(keys.getPublic());
		System.out.println(encPubKey);
		
		var pubKey = KeyEncoder.decodePublicKey(encPubKey);
		
		var result = Signing.verify("hello world", signature, pubKey);
		System.out.println(result);
	}
}
