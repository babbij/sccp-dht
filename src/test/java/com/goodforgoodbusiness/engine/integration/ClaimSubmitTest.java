package com.goodforgoodbusiness.engine.integration;

import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.sparql.util.NodeFactoryExtra.createLiteralNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.model.Link;
import com.goodforgoodbusiness.model.Link.RelType;
import com.goodforgoodbusiness.model.SubmittableClaim;
import com.goodforgoodbusiness.shared.encode.JSON;
import com.goodforgoodbusiness.webapp.ContentType;

public class ClaimSubmitTest {
	public static void main(String[] args) throws Exception {
		var submittedClaim = new SubmittableClaim();
		
		submittedClaim.added(
			new Triple(
				createURI("https://twitter.com/ijmad"),
				createURI("http://xmlns.com/foaf/0.1/name"),
				createLiteralNode("Ian Maddison", null, "http://www.w3.org/2001/XMLSchema#string")
			)
		);
		
		submittedClaim.linked(new Link(
			"b62accf26d5a1d8a7cb320e689ae2dd189a18cc3dca9457194e3d304e912c51d" +
			"adf746293e4707ec23a049e2cdb5684b2dcff91f5883e576d6a81100bafa56e4",
			RelType.CAUSED_BY
		));

		var httpClient = 
			HttpClient.newBuilder().build();

		var request = HttpRequest
			.newBuilder(new URI("http://localhost:8090/claims"))
			.header("Content-Type", ContentType.json.getContentTypeString())
			.POST(BodyPublishers.ofString(JSON.encode(submittedClaim).toString()))
			.build();
		
		var response = httpClient.send(request, BodyHandlers.ofString());
		
		System.out.println(response.statusCode());
	}
}
