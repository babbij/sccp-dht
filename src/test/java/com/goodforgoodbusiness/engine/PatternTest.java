package com.goodforgoodbusiness.engine;

import java.util.Optional;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.util.NodeFactoryExtra;

import com.goodforgoodbusiness.engine.store.keys.spec.ShareKeySpec;

public class PatternTest {
	public static void main(String[] args) {
		System.out.println(
			Pattern.forSearch(new Triple(
				NodeFactory.createURI("https://twitter.com/ijmad8x"),
				NodeFactory.createURI("http://xmlns.com/foaf/0.1/name"),
				Node.ANY
			))
		);
		
		System.out.println(
			Pattern.forSpec(new ShareKeySpec(
				Optional.of("https://twitter.com/ijmad8x"),
				Optional.of("http://xmlns.com/foaf/0.1/name"),
				Optional.empty()
			))
		);
		
		System.out.println(
			Pattern.forPublish(
				new Triple(
					NodeFactory.createURI("https://twitter.com/ijmad8x"),
					NodeFactory.createURI("http://xmlns.com/foaf/0.1/name"),
					NodeFactoryExtra.createLiteralNode("Hello", null, "http://www.w3.org/2001/XMLSchema/string")
				)
			)
		);
	}
}
