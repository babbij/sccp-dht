package com.goodforgoodbusiness.engine.store.container.impl;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.util.NodeFactoryExtra;

import com.goodforgoodbusiness.engine.ContainerBuilder;
import com.goodforgoodbusiness.engine.crypto.AsymmetricEncryption;
import com.goodforgoodbusiness.engine.crypto.Identity;
import com.goodforgoodbusiness.engine.store.container.impl.MemContainerStore;
import com.goodforgoodbusiness.model.SubmittableContainer;
import com.goodforgoodbusiness.model.TriTuple;

public class MemContainerStoreTest {
	public static void main(String[] args) throws Exception {
		var store = new MemContainerStore();
		
		var kp = AsymmetricEncryption.createKeyPair();
		var id = new Identity("foo", kp.getPrivate().toEncodedString(), kp.getPublic().toEncodedString());
		var containerBuilder = new ContainerBuilder(id);
		
		var trup = new Triple(
			NodeFactory.createURI("https://twitter.com/ijmad"),
			NodeFactory.createURI("http://xmlns.com/foaf/0.1/name"),
			NodeFactoryExtra.createLiteralNode("Ian Maddison", null, "http://www.w3.org/2001/XMLSchema#string")
		);
		
		var submittedContainer = new SubmittableContainer();
		submittedContainer.added(trup);
		
		var storedContainer = containerBuilder.buildFrom(submittedContainer);
		store.save(storedContainer);
		
		store.searchForPattern(TriTuple.from(trup)).forEach(c -> {
			System.out.println(c);
			c.getAdded().forEach(triple -> {
				System.out.println(triple.getSubject());
				System.out.println(triple.getPredicate());
				System.out.println(triple.getObject() + " (" + triple.getObject().getLiteralDatatypeURI() + ")");
			});
		});
	}
}
