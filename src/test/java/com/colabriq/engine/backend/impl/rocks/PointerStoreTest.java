package com.goodforgoodbusiness.engine.backend.impl.rocks;

import java.util.Arrays;

import com.goodforgoodbusiness.rocks.RocksManager;

public class PointerStoreTest {
	public static void main(String[] args) throws Exception {
		var manager = new RocksManager("db/pointers");
		manager.start();
		
		var store = new PointerStore(manager);
		
		store.publishPointer("abc123", new byte [] { 1, 1, 1, 1, 1, 1, 1 });
		store.publishPointer("abc123", new byte [] { 2, 2, 2, 2, 2 });
		store.publishPointer("def456", new byte [] { 3, 3, 3 });
		
		store.searchForPointers("abc123")
			.forEach(result -> System.out.println("abc123: " + Arrays.toString(result)));
		
		store.searchForPointers("def456")
			.forEach(result -> System.out.println("def456: " + Arrays.toString(result)));
		
		store.searchForPointers("ghi789")
			.forEach(result -> System.out.println("ghi789: " + Arrays.toString(result)));
	}
}
