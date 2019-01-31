package com.goodforgoodbusiness.engine.store.keys.spec;

import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.sparql.util.NodeFactoryExtra.createLiteralNode;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.shared.encode.JSON;

public class ShareKeySpecTest {
	public static void main(String[] args) {
		var s = createURI("https://twitter.com/ijmad8x");
		var p = createURI("http://xmlns.com/foaf/0.1/name");
		var o = createLiteralNode("Ian Maddison", null, "http://www.w3.org/2001/XMLSchema#string");
		
		var spec1 = new ShareKeySpec(new Triple(s, p, o));
		System.out.println(JSON.encode(spec1));
		
		var spec2 = new ShareKeySpec(new Triple(s, Node.ANY, o));
		System.out.println(JSON.encode(spec2));
	}
}
