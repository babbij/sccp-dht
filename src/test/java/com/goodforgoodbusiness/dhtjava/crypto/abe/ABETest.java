package com.goodforgoodbusiness.dhtjava.crypto.abe;

import java.security.KeyPair;

public class ABETest {
	public static void main(String[] args) throws Exception {
		ABE abe = ABE.newKeys();
		
		for (int i = 0; i < 1000; i++) {
			String cipherText = abe.encrypt("this is a test of the ABE library", "foo|bar|baz");
			System.out.println(cipherText);
			
			KeyPair shareKey = abe.shareKey("bar");
			
			String clearText = ABE.decrypt(cipherText, shareKey);
			System.out.println(clearText);
		}
		
		System.out.println("DONE!");
	}
}
