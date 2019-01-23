package com.goodforgoodbusiness.dhtjava.crypto;

import static com.goodforgoodbusiness.dhtjava.crypto.Symmetric.decrypt;
import static com.goodforgoodbusiness.dhtjava.crypto.Symmetric.encrypt;

import com.goodforgoodbusiness.dhtjava.crypto.KeyEncoder;
import com.goodforgoodbusiness.dhtjava.crypto.Symmetric;

public class SymmetricTest {
	public static void main(String[] args) throws Exception {
		var key = Symmetric.generateKey();
		
		var key64 = KeyEncoder.encodeKey(key);
		System.out.println(key64);

		var encrypted = encrypt(key, "Hello World this is a test of doing some encryption");
        System.out.println(encrypted);

        System.out.println(decrypt(KeyEncoder.decodeSecretKey(key64), encrypted));
	}
}
