package com.goodforgoodbusiness.engine.store.claim.impl;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Indexes.ascending;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.jena.graph.Triple;
import org.apache.log4j.Logger;
import org.bson.Document;

import com.goodforgoodbusiness.engine.Pattern;
import com.goodforgoodbusiness.engine.store.claim.ClaimStore;
import com.goodforgoodbusiness.model.StoredClaim;
import com.goodforgoodbusiness.shared.encode.JSON;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.ReplaceOptions;

@Singleton
public class MongoClaimStore implements ClaimStore {
	private static final Logger log = Logger.getLogger(MongoClaimStore.class);
	
	private static final String CL_INDEX = "index";
	private static final String CL_CLAIM = "claim";
	
	private final MongoClient client;
	private final ConnectionString connectionString;
	private final MongoDatabase database;
	
	@Inject
	public MongoClaimStore(@Named("claimstore.connectionUrl") String connectionUrl) {
		this.connectionString = new ConnectionString(connectionUrl);
		this.client =  MongoClients.create(connectionString);
		this.database = client.getDatabase(connectionString.getDatabase());
		
		var indexCollection = database.getCollection(CL_INDEX);
		indexCollection.createIndex(ascending("pattern"));

		var claimCollection = database.getCollection(CL_CLAIM);
		claimCollection.createIndex(ascending("inner_envelope.hashkey"), new IndexOptions().unique(true));
	}
	
	@Override
	public void save(StoredClaim claim) {
		log.debug("Put claim: " + claim.getId());
		
		// store claim as full JSON document
		database
			.getCollection(CL_CLAIM)
			.replaceOne(
				eq("inner_envelope.hashkey", claim.getId()),
				Document.parse(JSON.encodeToString(claim)),
				new ReplaceOptions().upsert(true)
			);
		
		// store patterns as pointers, similar to DHT
		// we can recalculate the patterns since the claim is fully unencrypted.
		claim
			.getTriples()
			.map(Pattern::forPublish)
			.flatMap(Set::stream)
			.forEach(pattern -> { 
				database
					.getCollection(CL_INDEX)
					.insertOne(
						new Document()
							.append("pattern", pattern)
							.append("claim", claim.getId())
					);
			});
		;
	}

	@Override
	public Stream<StoredClaim> search(Triple triple) {
		// find any pointers for the pattern
		return 
			StreamSupport.stream(
				database
					.getCollection(CL_INDEX)
					.find(eq("pattern", Pattern.forSearch(triple)))
					.spliterator(),
				true
			)
			.map(doc -> doc.getString("claim"))
			.map(this::getClaim)
			.filter(Objects::nonNull)
		;
	}
	
	private StoredClaim getClaim(String id) {
		Document doc = database
			.getCollection(CL_CLAIM)
		 	.find(eq("inner_envelope.hashkey", id))
		 	.first();
		
		if (doc != null) {
			return JSON.decode(doc.toJson(), StoredClaim.class);
		}
		else {
			return null;
		}
	}
	
	@Override
	public boolean contains(String claimId) {
		long count = database
			.getCollection(CL_CLAIM)
			.countDocuments(
				eq("inner_envelope.hashkey", claimId)
			);
		
		return count > 0;
	}
}
