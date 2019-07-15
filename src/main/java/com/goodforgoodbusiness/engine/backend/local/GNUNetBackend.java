//package com.goodforgoodbusiness.engine.backend.impl;
//
//import static java.util.stream.StreamSupport.stream;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpRequest.BodyPublishers;
//import java.net.http.HttpResponse.BodyHandlers;
//import java.util.Optional;
//import java.util.Set;
//import java.util.stream.Stream;
//
//import org.apache.log4j.Logger;
//
//import com.goodforgoodbusiness.engine.backend.DHTBackend;
//import com.goodforgoodbusiness.shared.URIModifier;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.google.gson.JsonArray;
//import com.google.gson.JsonObject;
//import com.google.gson.JsonParser;
//import com.google.inject.Inject;
//import com.google.inject.Singleton;
//import com.google.inject.name.Named;
//
///**
// * Integrate with our GNUNetService's JSON interface.
// */
//@Singleton
//public class GNUNetBackend implements DHTBackend {
//	private static final Logger log = Logger.getLogger(GNUNetBackend.class);
//	
//	private static final Gson GSON =  
//		new GsonBuilder()
//			.setPrettyPrinting()
//			.disableHtmlEscaping()
//			.create();
//	
//	private final HttpClient client;
//	private final URI endpoint;
//
//	@Inject
//	public GNUNetBackend(@Named("gnunet.endpoint") String gnunetEndpoint) throws URISyntaxException {
//		this.client = HttpClient.newBuilder().build();
//		this.endpoint = new URI(gnunetEndpoint);
//	}
//	
//	@Override
//	public Optional<String> publish(Set<String> keywords, String data) {
//		log.info("Publishing data with keywords " + keywords);
//		
//		try {
//			// {
//		    //   "keywords": ["daisy", "daisy"],
//			//   "data": "give me your answer do"
//			// }
//			
//			var k = new JsonArray();
//			keywords.forEach(keyword -> k.add(keyword));
//			
//			var b = new JsonObject();
//			b.addProperty("data", data);
//			b.add("keywords", k);
//			
//			var request = HttpRequest
//				.newBuilder(
//					URIModifier.from(endpoint)
//						.appendPath("publish")
//						.build()
//				)
//				.header("Content-Type", "application/json")
//				.POST(BodyPublishers.ofString(GSON.toJson(b)))
//				.build();
//			
//			
//			var response = client.send(request, BodyHandlers.ofString());
//			if (response.statusCode() == 200) {
//				//{
//				//  "location": "gnunet://fs/chk/4C6FX..."
//				//}
//				
//				var o = new JsonParser().parse(response.body()).getAsJsonObject();
//				return Optional.of(o.get("location").getAsString());
//			}
//			else {
//				log.error("Publish request failed: " + response.statusCode() + " (" + response.body() + ")");
//				return Optional.empty();
//			}
//		}
//		catch (IOException | InterruptedException | URISyntaxException e) {
//			log.error("Publish request failed (" + e.getMessage() + ")", e);
//			return Optional.empty();
//		}
//	}
//
//	@Override
//	public Stream<String> search(String keyword) {
//		log.info("Searching for data with keywords " + keyword);
//		
//		try {
//			var request = HttpRequest
//				.newBuilder(
//					URIModifier.from(endpoint)
//						.appendPath("search")
//						.addParam("keyword", keyword)
//						.build()
//				)
//				.header("Content-Type", "application/json")
//				.GET()
//				.build();
//						
//			var response = client.send(request, BodyHandlers.ofString());
//			if (response.statusCode() == 200) {
//				// {
//				//   "results": [
//				//     "gnunet://fs/chk/4C6FX2F..."
//				//	 ]
//				// }
//				
//				var o = new JsonParser().parse(response.body()).getAsJsonObject();
//				var a = o.get("results").getAsJsonArray();
//				
//				return stream(a.spliterator(), true)
//					.map(j -> j.getAsString())
//				;
//			}
//			else {
//				log.error("Search request failed: " + response.statusCode() + " (" + response.body() + ")");
//				return Stream.empty();
//			}
//		}
//		catch (IOException | InterruptedException | URISyntaxException e) {
//			log.error("Search request failed (" + e.getMessage() + ")", e);
//			return Stream.empty();
//		}
//	}
//
//	@Override
//	public Optional<String> fetch(String location) {
//		log.info("Fetching location " + location);
//		
//		try {
//			var request = HttpRequest
//				.newBuilder(
//					URIModifier.from(endpoint)
//						.appendPath("fetch")
//						.addParam("location", location)
//						.build()
//				)
//				.header("Content-Type", "application/json")
//				.GET()
//				.build();
//			
//			var response = client.send(request, BodyHandlers.ofString());
//			if (response.statusCode() == 200) {
//				// {
//				//   "data": "give me your answer do"
//				// }
//				
//				var o = new JsonParser().parse(response.body()).getAsJsonObject();
//				return Optional.of(o.get("data").getAsString());
//			}
//			else {
//				log.error("Fetch request failed: " + response.statusCode() + " (" + response.body() + ")");
//				return Optional.empty();
//			}
//		}
//		catch (IOException | InterruptedException | URISyntaxException e) {
//			log.error("Fetch request failed (" + e.getMessage() + ")", e);
//			return Optional.empty();
//		}
//	}
//}
