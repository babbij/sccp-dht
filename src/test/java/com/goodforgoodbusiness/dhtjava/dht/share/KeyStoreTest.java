package com.goodforgoodbusiness.dhtjava.dht.share;

import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.sparql.util.NodeFactoryExtra.createLiteralNode;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.dhtjava.dht.share.impl.MongoKeyStore;
import com.goodforgoodbusiness.kpabe.KPABEInstance;

public class KeyStoreTest {
	public static void main(String[] args) throws Exception {
		var abe = KPABEInstance.newKeys();
		
		var s = createURI("https://twitter.com/ijmad8x");
		var p = createURI("http://xmlns.com/foaf/0.1/name");
		var o = createLiteralNode("Ian Maddison", null, "http://www.w3.org/2001/XMLSchema#string");
		

		var ks = new MongoKeyStore("mongodb://localhost:27017/keys");
		
		ks.saveKey(new Triple(s, p, o), abe.shareKey("foo"));
		
		ks.findKey(new Triple(s, p, o)).forEach(
			shareKey -> {
				System.out.println(shareKey.getPublic());
				System.out.println(shareKey.getPrivate());
				System.out.println();
			}
		);
	}
}
