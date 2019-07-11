//package com.goodforgoodbusiness.engine.backend.impl;
//
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Optional;
//import java.util.Set;
//import java.util.stream.Stream;
//
//import com.goodforgoodbusiness.engine.backend.DHTBackend;
//import com.goodforgoodbusiness.shared.encode.Hash;
//import com.goodforgoodbusiness.shared.encode.Hex;
//import com.google.inject.Singleton;
//
//@Singleton
//public class MemDHTBackend implements DHTBackend { 
//	private Map<String, Set<String>> keysMap = new HashMap<>();
//	private Map<String, String> dataMap = new HashMap<>();
//
//	@Override
//	public Optional<String> publish(Set<String> keywords, String data) {
//		String location = Hex.encode(Hash.sha512(data.getBytes()));
//		dataMap.put(location, data);
//		
//		keywords.forEach(keyword -> {
//			var existing = keysMap.get(keyword);
//			if (existing != null) {
//				existing.add(location);
//			}
//			else {
//				var newSet = new HashSet<String>();
//				newSet.add(location);
//				keysMap.put(keyword, newSet);
//			}
//		});
//		
//		return Optional.of(location);
//	}
//	
//	@Override
//	public Stream<String> search(String keyword) {
//		return Optional
//			.ofNullable(keysMap.get(keyword))
//			.stream() // stream so it's just non-empty Optionals
//			.flatMap(Set::stream)
//		;
//	}
//	
//	@Override
//	public Optional<String> fetch(String location) {
//		return Optional.ofNullable(dataMap.get(location));
//	}
//}
