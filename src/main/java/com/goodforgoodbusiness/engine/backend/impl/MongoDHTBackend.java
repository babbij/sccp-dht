package com.goodforgoodbusiness.engine.backend.impl;

import static com.mongodb.client.model.Filters.eq;
import static java.util.stream.StreamSupport.stream;
import static com.mongodb.client.model.Indexes.ascending;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.goodforgoodbusiness.engine.backend.DHTBackend;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@Singleton
public class MongoDHTBackend implements DHTBackend {
	private static final Logger log = Logger.getLogger(MongoDHTBackend.class);
	
	private static final String CL_KEYS = "keys";
	private static final String CL_DATA = "data";
	
	private final MongoClient client;
	private final ConnectionString connectionString;
	private final MongoDatabase database;
	
	@Inject
	public MongoDHTBackend(@Named("dhtstore.connectionUrl") String connectionUrl) {
		this.connectionString = new ConnectionString(connectionUrl);
		this.client =  MongoClients.create(connectionString);
		this.database = client.getDatabase(connectionString.getDatabase());
		
		keys().createIndex(ascending("key"));
	}
	
	private MongoCollection<Document> keys() {
		return database.getCollection(CL_KEYS);
	}
	
	private MongoCollection<Document> data() {
		return database.getCollection(CL_DATA);
	}

	@Override
	public Optional<String> publish(Set<String> keywords, String data) {
		var doc = new Document("data", data);
		data().insertOne(doc);
		
		var docId = (ObjectId)doc.get( "_id" );
		log.debug("Published doc " + docId);
		
		keywords.forEach(keyword -> {
			log.debug("Keyword " + keyword + " -> " + docId);
			keys().insertOne(new Document("key", keyword).append("doc", docId));
		});
		
		return Optional.of(docId.toHexString());
	}
	
	@Override
	public Stream<String> search(String keyword) {
		log.debug("Search for keyword " + keyword);
		
		return 
			stream(
				keys().find( eq("key", keyword) ).spliterator(),
				true
			)
			.map(doc -> ((ObjectId)doc.get("doc")).toHexString())
		;
	}

	@Override
	public Optional<String> fetch(String location) {
		log.debug("Fetch doc " + location);
		
		return Optional
			.ofNullable(data().find(eq("_id", new ObjectId(location))).first())
			.map(doc -> doc.get("data").toString())
		;
	}
}
