package com.goodforgoodbusiness.engine.store.container.impl;

import static java.lang.System.out;

import com.goodforgoodbusiness.engine.store.container.impl.MongoContainerStore;

public class MongoContainerStoreListTest {
	public static void main(String[] args) throws Exception {
		System.out.println("----------");
		
		var store1 = new MongoContainerStore("mongodb://localhost:27017/containerstore1");
		store1.allContainers().forEach(out::println);
		
		System.out.println("----------");
		
		var store2 = new MongoContainerStore("mongodb://localhost:27017/containerstore2");
		store2.allContainers().forEach(out::println);
		
		System.out.println("----------");
	}
}
