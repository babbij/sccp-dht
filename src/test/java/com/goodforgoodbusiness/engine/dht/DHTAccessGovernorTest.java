package com.goodforgoodbusiness.engine.dht;

import static org.apache.jena.graph.Node.ANY;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.sparql.util.NodeFactoryExtra.createLiteralNode;

import java.time.Duration;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.engine.Governer;
import com.goodforgoodbusiness.model.TriTuple;

public class DHTAccessGovernorTest {
	public static void main(String[] args) {
		var sub = createURI("https://twitter.com/ijmad");
		var pre = createURI("http://xmlns.com/foaf/0.1/name");
		var obj = createLiteralNode("Ian Maddison", null, "http://www.w3.org/2001/XMLSchema#string");
		
		var trip1 = TriTuple.from(new Triple(sub, pre, obj));
		var trip2 = TriTuple.from(new Triple(sub, pre, ANY));
		
		var gov1 = new Governer(true, Duration.ofSeconds(30));
		
		System.out.println(gov1.allow(trip1)); // true
		System.out.println(gov1.allow(trip1)); // false
		
		gov1.invalidate(trip1); // true
		
		System.out.println(gov1.allow(trip1));
		
		// also try a narrowing search
		
		var gov2 = new Governer(true, Duration.ofSeconds(30));
		
		System.out.println(gov2.allow(trip2)); // true
		System.out.println(gov2.allow(trip1)); // false
		
		// but the inverse shouldn't be true
		
		var gov3 = new Governer(true, Duration.ofSeconds(30));
		
		System.out.println(gov3.allow(trip1)); // true
		System.out.println(gov3.allow(trip2)); // true
	}
}

