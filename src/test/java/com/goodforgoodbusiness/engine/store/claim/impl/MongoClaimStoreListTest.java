package com.goodforgoodbusiness.engine.store.claim.impl;

import static java.lang.System.out;

public class MongoClaimStoreListTest {
	public static void main(String[] args) throws Exception {
		System.out.println("----------");
		
		var store1 = new MongoClaimStore("mongodb://localhost:27017/claimstore1");
		store1.allClaims().forEach(out::println);
		
		System.out.println("----------");
		
		var store2 = new MongoClaimStore("mongodb://localhost:27017/claimstore2");
		store2.allClaims().forEach(out::println);
		
		System.out.println("----------");
	}
}
