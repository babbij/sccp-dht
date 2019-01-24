package com.goodforgoodbusiness.dhtjava.crypto;

import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.sparql.util.NodeFactoryExtra.createLiteralNode;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.dhtjava.Pattern;
import com.goodforgoodbusiness.dhtjava.crypto.primitive.key.EncodeableShareKey;
import com.goodforgoodbusiness.dhtjava.crypto.store.impl.MemoryKeyStore;
import com.goodforgoodbusiness.dhtjava.crypto.store.spec.ShareKeySpec;
import com.goodforgoodbusiness.kpabe.KPABEInstance;
import com.goodforgoodbusiness.model.Pointer;

public class PointerCrypterTest {
	public static void main(String[] args) throws Exception {
		var triple = new Triple(
			createURI("https://twitter.com/ijmad8x"),
			createURI("http://xmlns.com/foaf/0.1/name"),
			createLiteralNode("Ian Maddison", null, "http://www.w3.org/2001/XMLSchema#string")
		);
		
		var kpabe = KPABEInstance.newKeys();
		var store = new MemoryKeyStore();
		var crypter = new PointerCrypter(kpabe, store);
		
		crypter.saveShareKey(new ShareKeySpec(triple), new EncodeableShareKey(kpabe.shareKey(Pattern.forSearch(triple))));
				
		var key = new ClaimCrypter().getSecretKey();
		var pointer = new Pointer("abc123", key.toEncodedString(), 1234l);
		var data = crypter.encrypt(pointer, Pattern.forPublish(triple));
		
		System.out.println(data);
		
		var pointer2 = crypter.decrypt(triple, data);
		System.out.println(pointer2);
	}
}
