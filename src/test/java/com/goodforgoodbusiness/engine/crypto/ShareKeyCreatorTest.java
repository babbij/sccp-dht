package com.goodforgoodbusiness.engine.crypto;

import static java.time.ZonedDateTime.now;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.sparql.util.NodeFactoryExtra.createLiteralNode;

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.engine.AttributeMaker;
import com.goodforgoodbusiness.kpabe.local.KPABELocalInstance;
import com.goodforgoodbusiness.model.TriTuple;

public class ShareKeyCreatorTest {
	public static void main(String[] args) throws Exception {
		var tt = TriTuple.from(
			new Triple(
				createURI("https://twitter.com/ijmad8x"),
				createURI("http://xmlns.com/foaf/0.1/name"),
				createLiteralNode("Ian Maddison", null, "http://www.w3.org/2001/XMLSchema/string")
			)
		);
		
		var kpabe = KPABELocalInstance.newKeys();
		var keyManager = new KeyManager(kpabe.getPublicKey(), kpabe.getSecretKey());
		
		var scc = new ShareKeyCreator(keyManager);
		
		var keyPair1 = scc.newKey(tt, Optional.empty(), Optional.empty());
		System.out.println(keyPair1);
		
		// get the attribute hash for the tuple
		var attributes = AttributeMaker.forPublish(keyManager.getPublicKey(), Stream.of(tt));
		
		// directly encrypt some data with it
		var ciphertext = kpabe.encrypt("testing testing", attributes);
		
		var cleartext = KPABELocalInstance.decrypt(ciphertext, keyPair1.toKeyPair());
		System.out.println(cleartext);
		
		// see if date ranges work
		var keyPair2 = scc.newKey(tt, Optional.of(now().minusDays(1)), Optional.of(now().plusDays(1)));
		System.out.println(KPABELocalInstance.decrypt(ciphertext, keyPair2.toKeyPair()));
		
		// provide a date range that won't cover the encryption attribute time
		var keyPair3 = scc.newKey(tt, Optional.of(now().minusDays(2)), Optional.of(now().minusDays(1)));
		System.out.println(KPABELocalInstance.decrypt(ciphertext, keyPair3.toKeyPair())); // should be null
	}
}
