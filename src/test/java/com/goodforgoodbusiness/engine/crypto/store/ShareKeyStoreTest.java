package com.goodforgoodbusiness.engine.crypto.store;

import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.sparql.util.NodeFactoryExtra.createLiteralNode;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.engine.crypto.primitive.key.EncodeableShareKey;
import com.goodforgoodbusiness.engine.store.keys.impl.MongoKeyStore;
import com.goodforgoodbusiness.engine.store.keys.spec.ShareKeySpec;
import com.goodforgoodbusiness.kpabe.KPABEInstance;

public class ShareKeyStoreTest {
	public static void main(String[] args) throws Exception {
		var abe = KPABEInstance.newKeys();
		var shareKey = new EncodeableShareKey(abe.shareKey("foo"));
		
		var s = createURI("https://twitter.com/ijmad8x");
		var p = createURI("http://xmlns.com/foaf/0.1/name");
		var o = createLiteralNode("Ian Maddison", null, "http://www.w3.org/2001/XMLSchema#string");
		

		var ks = new MongoKeyStore("mongodb://localhost:27017/keys");
		
		ks.saveKey(new ShareKeySpec(new Triple(s, p, o)), shareKey);
		
		ks.findKeys(new ShareKeySpec(new Triple(s, p, o))).forEach(
			foundKey -> {
				System.out.println(foundKey.toKeyPair().getPublic());
				System.out.println(foundKey.toKeyPair().getPrivate());
				System.out.println();
			}
		);
	}
}
