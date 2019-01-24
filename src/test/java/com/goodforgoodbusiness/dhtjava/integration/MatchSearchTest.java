package com.goodforgoodbusiness.dhtjava.integration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.shared.encode.JSON;
import com.goodforgoodbusiness.shared.web.URIModifier;

public class MatchSearchTest {
	public static void main(String[] args) throws Exception {
		for (int i = 0; i < 100; i++) {
			var trup = new Triple(
				NodeFactory.createURI("https://twitter.com/ijmad"),
				NodeFactory.createURI("http://xmlns.com/foaf/0.1/name"),
				Node.ANY
			);
			
			var httpClient = 
				HttpClient.newBuilder().build();
			
			var uri = URIModifier
				.from(new URI("http://localhost:8090/matches"))
				.addParam("pattern", JSON.encode(trup).toString())
				.build();
				
			var request = HttpRequest
				.newBuilder(uri)
				.GET()
				.build();
				
			var response = httpClient.send(request, BodyHandlers.ofString());
			
			if (response.statusCode() == 200) {
				System.out.println(response.body());
			}
			else {
				System.err.println("DHT response was " + response.statusCode());
			}
		}
	}
}
