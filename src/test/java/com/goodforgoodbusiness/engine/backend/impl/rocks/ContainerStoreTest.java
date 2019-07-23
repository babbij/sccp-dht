package com.goodforgoodbusiness.engine.backend.impl.rocks;

import java.util.Arrays;

import com.goodforgoodbusiness.rocks.RocksManager;

public class ContainerStoreTest {
	public static void main(String[] args) throws Exception {
		var manager = new RocksManager("db/containers");
		manager.start();
		
		var store = new ContainerStore(manager);
		
		var location1 = store.publishContainer("abc123", new byte [] { 1, 1, 1, 1, 1, 1, 1 });
		System.out.println("PUBLISHED: " + location1);
		
		store.searchForContainer("abc123").forEach(
			foundLocation -> {
				System.out.println("FOUND: " + foundLocation);
				
				try {
					var result = store.fetchContainer(foundLocation);
					System.out.println("DATA: " + Arrays.toString(result.get()));
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		);
		
//		var location2 = store.publishContainer("def456", new byte [] { 2, 2, 2 });
//		System.out.println(location2);
//		
//		var location3 = store.publishContainer("ghi789", new byte [] { 3, 3, 3, 3, 3 });
//		System.out.println(location3);
	}
}
