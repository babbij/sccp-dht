package com.goodforgoodbusiness.engine.crypto;

import com.goodforgoodbusiness.kpabe.local.KPABELocalInstance;

public class KPABETest {
	public static void main(String[] args) throws Exception {
		var instance = KPABELocalInstance.newKeys();
		
		System.out.println("public = " + instance.getPublicKey().toString());
		
		System.out.println("secret = " + instance.getSecretKey().toString());
	}
}
