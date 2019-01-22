package com.goodforgoodbusiness.dhtjava.crypto.abe;

public class ABEKeys {
	public static void main(String[] args) throws Exception {
		ABE abe = ABE.newKeys();
		
		System.out.println("public = " + abe.getPublicKey().toString());
		System.out.println("secret = " + abe.getSecretKey().toString());
	}
}
