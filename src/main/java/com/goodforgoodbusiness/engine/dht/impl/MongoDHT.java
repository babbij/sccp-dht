package com.goodforgoodbusiness.engine.dht.impl;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Indexes.ascending;
import static java.util.stream.StreamSupport.stream;

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.bson.Document;

import com.goodforgoodbusiness.engine.dht.DHT;
import com.goodforgoodbusiness.engine.dht.DHTPointer;
import com.goodforgoodbusiness.engine.dht.DHTPointerMeta;
import com.goodforgoodbusiness.model.EncryptedClaim;
import com.goodforgoodbusiness.shared.encode.JSON;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;

@Singleton
public class MongoDHT implements DHT {
	private static final Logger log = Logger.getLogger(MongoDHT.class);
	
	// Mongo doesn't need meta but we can use a single instance as a type check
	protected static final DHTPointerMeta META = new DHTPointerMeta();
	
	private static final String CL_INDEX = "index";
	private static final String CL_CLAIM = "claim";
	
	private final MongoClient client;
	private final ConnectionString connectionString;
	private final MongoDatabase database;
	
	@Inject
	public MongoDHT(@Named("dhtstore.connectionUrl") String connectionUrl) {
		this.connectionString = new ConnectionString(connectionUrl);
		this.client =  MongoClients.create(connectionString);
		this.database = client.getDatabase(connectionString.getDatabase());
		
		var indexCollection = database.getCollection(CL_INDEX);
		indexCollection.createIndex(ascending("pattern"));

		var claimCollection = database.getCollection(CL_CLAIM);
		claimCollection.createIndex(ascending("inner_envelope.hashkey"), new IndexOptions().unique(true));
	}
	
	@Override
	public Stream<DHTPointer> getPointers(String pattern) {
		log.debug("Get pointers: " + pattern);
		
		return 
			stream(
				database
					.getCollection(CL_INDEX)
					.find(eq("pattern", pattern))
					.spliterator(),
				true
			)
			.map(doc -> new DHTPointer(doc.get("data").toString(), META))
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
	public Optional<EncryptedClaim> getClaim(String id, DHTPointerMeta meta) {
		log.debug("Get claim: " + id);
		
		if (meta == META) {
			return 
				Optional.ofNullable(
					database
						.getCollection(CL_CLAIM)
					 	.find(eq("inner_envelope.hashkey", id))
					 	.first()
				)
				.map(doc -> JSON.decode(doc.toJson(), EncryptedClaim.class))
			;
		}
		else {
			return Optional.empty();
		}
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
