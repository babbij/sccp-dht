package com.goodforgoodbusiness.dhtjava.crypto;

import com.goodforgoodbusiness.dhtjava.crypto.Identity;
import com.goodforgoodbusiness.dhtjava.crypto.primitive.AsymmetricEncryption;

public class IdentityTest {
	public static void main(String[] args) throws Exception {
		var kp = AsymmetricEncryption.createKeyPair();
		
		System.out.println("private = " + kp.getPrivate().toEncodedString());
		System.out.println("public = " + kp.getPublic().toEncodedString());
		
		var id = new Identity("blah", kp.getPrivate().toEncodedString(), kp.getPublic().toEncodedString());
		
		var sig = id.sign("foo");
		
		System.out.println(id.verify("foo", sig));
	}
}
