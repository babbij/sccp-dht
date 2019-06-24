package com.goodforgoodbusiness.engine;

import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.sparql.util.NodeFactoryExtra.createLiteralNode;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.kpabe.KPABEKeyManager;
import com.goodforgoodbusiness.model.TriTuple;

public class AttributeMakerTest {
	public static void main(String[] args) throws Exception {
		var keys = KPABEKeyManager.newKeys();
		
		String attributes = Attributes.forPublish(
			keys.getPublic(),
			Stream.of(
				TriTuple.from(
					new Triple(
						createURI("https://twitter.com/ijmad8x"),
						createURI("http://xmlns.com/foaf/0.1/name"),
						createLiteralNode("Ian Maddison", null, "http://www.w3.org/2001/XMLSchema/string")
					)
				),
				
				TriTuple.from(
					new Triple(
						createURI("https://twitter.com/ijmad8x"),
						createURI("http://xmlns.com/foaf/0.1/age"),
						createLiteralNode("35", null, "http://www.w3.org/2001/XMLSchema/integer")
					)
				)
			)
		);
		
		System.out.println(attributes);
		
		var share1 = Attributes.forShare(
			keys.getPublic(),
			TriTuple.from(
				new Triple(
					createURI("https://twitter.com/ijmad8x"),
					createURI("http://xmlns.com/foaf/0.1/name"),
					createLiteralNode("Ian Maddison", null, "http://www.w3.org/2001/XMLSchema/string")
				)
			), 
			Optional.empty(), 
			Optional.empty()
		);
		
		System.out.println(share1);
		
		var share2 = Attributes.forShare(
			keys.getPublic(),
			TriTuple.from(
				new Triple(
					createURI("https://twitter.com/ijmad8x"),
					createURI("http://xmlns.com/foaf/0.1/name"),
					Node.ANY
				)
			), 
			Optional.empty(), 
			Optional.empty()
		);
		
		System.out.println(share2);
		
		var share3 = Attributes.forShare(
			keys.getPublic(),
			TriTuple.from(
				new Triple(
					createURI("https://twitter.com/ijmad8x"),
					createURI("http://xmlns.com/foaf/0.1/name"),
					Node.ANY
				)
			), 
			Optional.of(ZonedDateTime.now()), 
			Optional.of(ZonedDateTime.now().plusDays(1))
		);
		
		System.out.println(share3);
	}
}
