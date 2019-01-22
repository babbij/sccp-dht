package com.goodforgoodbusiness.dhtjava.dht.impl;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Indexes.ascending;
import static java.util.stream.StreamSupport.stream;

import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.bson.Document;

import com.goodforgoodbusiness.dhtjava.dht.DHTStore;
import com.goodforgoodbusiness.shared.JSON;
import com.goodforgoodbusiness.shared.model.EncryptedClaim;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;

public class MongoDHTStore implements DHTStore {
	private static final Logger log = Logger.getLogger(MongoDHTStore.class);
	
	private static final String CL_INDEX = "index";
	private static final String CL_CLAIM = "claim";
	
	private final MongoClient client;
	private final ConnectionString connectionString;
	private final MongoDatabase database;
	
	public MongoDHTStore(String connectionUrl) {
		this.connectionString = new ConnectionString(connectionUrl);
		this.client =  MongoClients.create(connectionString);
		this.database = client.getDatabase(connectionString.getDatabase());
		
		var indexCollection = database.getCollection(CL_INDEX);
		indexCollection.createIndex(ascending("pattern"));

		var claimCollection = database.getCollection(CL_CLAIM);
		claimCollection.createIndex(ascending("inner_envelope.hashkey"), new IndexOptions().unique(true));
	}
	
	@Override
	public Stream<String> getPointers(String pattern) {
		log.debug("Get pointers: " + pattern);
		
		return 
			stream(
				database
					.getCollection(CL_INDEX)
					.find(eq("pattern", pattern))
					.spliterator(),
				true
			)
			.map(doc -> doc.get("data").toString())
		;
	}

	@Override
	public void putPointer(String pattern, String data) {
		log.debug("Put pointer: " + pattern);
		
		database
			.getCollection(CL_INDEX)
			.insertOne(
				new Document()
					.append("pattern", pattern)
					.append("data", data)
			);
	}
	
	@Override
	public EncryptedClaim getClaim(String id) {
		log.debug("Get claim: " + id);
		
		return JSON.decode(
			database
				.getCollection(CL_CLAIM)
			 	.find(eq("inner_envelope.hashkey", id))
			 	.first()
			 	.toJson(),
			 EncryptedClaim.class
		);
	}

	@Override
	public void putClaim(EncryptedClaim claim) {
		log.debug("Put claim: " + claim.getId());
		
		database
			.getCollection(CL_CLAIM)
			.insertOne(
				Document.parse(JSON.encodeToString(claim))
			);
	}
}
