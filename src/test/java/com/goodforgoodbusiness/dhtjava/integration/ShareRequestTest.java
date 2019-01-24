package com.goodforgoodbusiness.dhtjava.integration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

import com.goodforgoodbusiness.shared.web.ContentType;

public class ShareRequestTest {
	public static void main(String[] args) throws Exception {
		var httpClient = 
			HttpClient.newBuilder().build();

		var request = HttpRequest
			.newBuilder(new URI("http://localhost:8080/share?sub=test1&obj=test3&start=2019-01-01T00:00"))
			.header("Content-Type", ContentType.json.getContentTypeString())
			.GET()
			.build();
		
		var response = httpClient.send(request, BodyHandlers.ofString());
		
		System.out.println(response.statusCode());
		
		System.out.println(response.body());
	}
}
