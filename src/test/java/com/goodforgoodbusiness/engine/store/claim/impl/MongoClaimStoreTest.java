package com.goodforgoodbusiness.engine.store.claim.impl;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.util.NodeFactoryExtra;

import com.goodforgoodbusiness.engine.ClaimBuilder;
import com.goodforgoodbusiness.engine.crypto.Identity;
import com.goodforgoodbusiness.engine.crypto.primitive.AsymmetricEncryption;
import com.goodforgoodbusiness.engine.store.claim.impl.MongoClaimStore;
import com.goodforgoodbusiness.model.SubmittableClaim;
import com.goodforgoodbusiness.model.TriTuple;

public class MongoClaimStoreTest {
	public static void main(String[] args) throws Exception {
		var store = new MongoClaimStore("mongodb://localhost:27017/claimstoretest");
		
		var kp = AsymmetricEncryption.createKeyPair();
		var id = new Identity("foo", kp.getPrivate().toEncodedString(), kp.getPublic().toEncodedString());
		var claimBuilder = new ClaimBuilder(id);
		
		var trup = new Triple(
			NodeFactory.createURI("https://twitter.com/ijmad"),
			NodeFactory.createURI("http://xmlns.com/foaf/0.1/name"),
			NodeFactoryExtra.createLiteralNode("Ian Maddison", null, "http://www.w3.org/2001/XMLSchema#string")
		);
		
		var submittedClaim = new SubmittableClaim();
		submittedClaim.added(trup);
		
		var storedClaim = claimBuilder.buildFrom(submittedClaim);
		store.save(storedClaim);
		
		store.search(TriTuple.from(trup)).forEach(c -> {
			System.out.println(c);
			c.getAdded().forEach(triple -> {
				System.out.println(triple.getSubject());
				System.out.println(triple.getPredicate());
				System.out.println(triple.getObject() + " (" + triple.getObject().getLiteralDatatypeURI() + ")");
			});
		});
	}
}
