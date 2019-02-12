package com.goodforgoodbusiness.engine.store.claim.impl;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Indexes.ascending;

import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.log4j.Logger;
import org.bson.Document;

import com.goodforgoodbusiness.engine.store.claim.ClaimStore;
import com.goodforgoodbusiness.model.StoredClaim;
import com.goodforgoodbusiness.model.TriTuple;
import com.goodforgoodbusiness.shared.encode.JSON;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
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
		
		// store patterns with all combinations, similar to DHT
		claim
			.getTriples()
			.flatMap(triple -> TriTuple.from(triple).matchingCombinations())
			.forEach(tuple -> { 
				database
					.getCollection(CL_INDEX)
					.insertOne(	
						new Document()
							.append("tuple", Document.parse(JSON.encodeToString(tuple)))
							.append("claim", claim.getId())
					);
			});
		;
	}

	@Override
	public Stream<StoredClaim> search(TriTuple tt) {
		// find any pointers for the pattern
		return 
			StreamSupport.stream(
				database
					.getCollection(CL_INDEX)
					.find(Filters.and(
						// search for tuple with correct signature
						eq("tuple.sub", tt.getSubject().orElse(null)),
						eq("tuple.pre", tt.getPredicate().orElse(null)),
						eq("tuple.obj", tt.getObject().orElse(null))
					))
					.spliterator(),
				true
			)
			.parallel()
			.map(doc -> doc.getString("claim"))
			.map(this::getClaim)
			.filter(Optional::isPresent)
			.map(Optional::get)
		;
	}
	
	public Optional<StoredClaim> getClaim(String id) {
		return 
			Optional.ofNullable(
				database
					.getCollection(CL_CLAIM)
				 	.find(eq("inner_envelope.hashkey", id))
				 	.first()
			)
			.map(doc -> JSON.decode(doc.toJson(), StoredClaim.class))
		;
	}
	
	protected Stream<StoredClaim> allClaims() {
		return 
			StreamSupport.stream(
				database
					.getCollection(CL_CLAIM)
				 	.find()
				 	.spliterator(),
				 true
			)
			.map(doc -> JSON.decode(doc.toJson(), StoredClaim.class))
		;
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
