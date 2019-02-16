package com.goodforgoodbusiness.engine.store.keys.impl;

import static java.util.stream.Collectors.toList;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.sparql.util.NodeFactoryExtra.createLiteralNode;

import java.util.Optional;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.engine.crypto.primitive.key.EncodeableShareKey;
import com.goodforgoodbusiness.kpabe.local.KPABELocalInstance;
import com.goodforgoodbusiness.model.TriTuple;

public class MemKeyStoreTest {
	public static void main(String[] args) throws Exception {
		var kpabe = KPABELocalInstance.newKeys();
		
		// make random sharekey
		var keyPair = kpabe.shareKey("foo");
		var encShareKey = new EncodeableShareKey(keyPair);		
		var store = new MemKeyStore();
		
		// save a key that would cover both tuples
		store.saveKey(
			new TriTuple(
				Optional.of("https://twitter.com/ijmad8x"),
				Optional.empty(),
				Optional.empty()
			),
			encShareKey
		);
		
		System.out.println("----------------------------------------");
		
		// check share key is returned when searching for right things
		var tt1 = TriTuple.from(
			new Triple(
				createURI("https://twitter.com/ijmad8x"),
				createURI("http://xmlns.com/foaf/0.1/name"),
				createLiteralNode("Ian Maddison", null, "http://www.w3.org/2001/XMLSchema/string")
			)
		);
		
		store.knownInfoCreators(tt1).forEach(r -> {
			System.out.println(r);
			System.out.println("⇒" + store.keysForDecrypt(r, tt1).collect(toList()));
		});
		
		System.out.println("----------------------------------------");
		
		var tt2 = TriTuple.from(
			new Triple(
				createURI("https://twitter.com/ijmad9x"),
				createURI("http://xmlns.com/foaf/0.1/age"),
				createLiteralNode("35", null, "http://www.w3.org/2001/XMLSchema/integer")
			)
		);
		
		// check key is not returned when searching for wrong things
		store.knownInfoCreators(tt2).forEach(r -> {
			System.out.println(r);
			System.out.println("⇒" + store.keysForDecrypt(r, tt2).collect(toList()));
		});
		
		System.out.println("----------------------------------------");
		
		var tt3 = TriTuple.from(
			new Triple(
				createURI("https://twitter.com/ijmad8x"),
				createURI("http://xmlns.com/foaf/0.1/name"),
				Node.ANY
			)
		);
		
		// check narrower but partial searches
		store.knownInfoCreators(tt3).forEach(r -> {
			System.out.println(r);
			System.out.println("⇒" + store.keysForDecrypt(r, tt3).collect(toList()));
		});
		
		System.out.println("----------------------------------------");
		
		var tt4 = TriTuple.from(
			new Triple(
				createURI("https://twitter.com/ijmad8x"),
				Node.ANY,
				Node.ANY
			)
		);
		
		store.knownInfoCreators(tt4).forEach(r -> {
			System.out.println(r);
			System.out.println("⇒" + store.keysForDecrypt(r, tt4).collect(toList()));
		});
		
		System.out.println("----------------------------------------");
	}
}