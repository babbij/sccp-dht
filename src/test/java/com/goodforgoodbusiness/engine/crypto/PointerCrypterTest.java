package com.goodforgoodbusiness.engine.crypto;

import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.sparql.util.NodeFactoryExtra.createLiteralNode;

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.engine.AttributeMaker;
import com.goodforgoodbusiness.engine.crypto.pointer.LocalPointerCrypter;
import com.goodforgoodbusiness.engine.crypto.primitive.key.EncodeableShareKey;
import com.goodforgoodbusiness.engine.store.keys.impl.MemKeyStore;
import com.goodforgoodbusiness.kpabe.local.KPABELocalInstance;
import com.goodforgoodbusiness.model.Pointer;
import com.goodforgoodbusiness.model.TriTuple;

public class PointerCrypterTest {
	public static void main(String[] args) throws Exception {
		// imagine A is sharing a claim with the following triples
		var tt1 = TriTuple.from(
			new Triple(
				createURI("https://twitter.com/ijmad8x"),
				createURI("http://xmlns.com/foaf/0.1/name"),
				createLiteralNode("Ian Maddison", null, "http://www.w3.org/2001/XMLSchema/string")
			)
		);
				
		var tt2 = TriTuple.from(
			new Triple(
				createURI("https://twitter.com/ijmad8x"),
				createURI("http://xmlns.com/foaf/0.1/age"),
				createLiteralNode("35", null, "http://www.w3.org/2001/XMLSchema/integer")
			)
		);
		
		// generate kpabe keys for A
		var kpabeA = KPABELocalInstance.newKeys();
		var keyManagerA = new KeyManager(kpabeA.getPublicKey(), kpabeA.getSecretKey());
		
		// pointer crypter for encrypt
		var crypterA = new LocalPointerCrypter(keyManagerA, new MemKeyStore());
		
		var claimKey = new ClaimCrypter().getSecretKey();
		
		// generate fake pointer
		var pointer = new Pointer("abc123", claimKey.toEncodedString(), 1234l);
		var data = crypterA.encrypt(pointer, AttributeMaker.forPublish(Stream.of(tt1, tt2)));
		
		System.out.println(data);
		
		// create fake share key
		var shareKey = new EncodeableShareKey(kpabeA.shareKey(AttributeMaker.forShare(tt1, Optional.empty(), Optional.empty())));
		
		// put it in a store created for B
		var storeB = new MemKeyStore();
		storeB.saveKey(tt1, shareKey);
		
		// create a pointer crypter around B's store
		var kpabeB = KPABELocalInstance.newKeys();
		var keyManagerB = new KeyManager(kpabeB.getPublicKey(), kpabeB.getSecretKey());
		var crypterB = new LocalPointerCrypter(keyManagerB, storeB);
		
		// see if B can decrypt, using A's identity (public key)
		var recoveredPointer = crypterB.decrypt(keyManagerA.getPublicKey(), data);
		System.out.println(recoveredPointer.get());
	}
}
