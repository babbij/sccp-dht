package com.goodforgoodbusiness.engine.store.container.impl;

import static com.goodforgoodbusiness.shared.TimingRecorder.timer;
import static com.goodforgoodbusiness.shared.TimingRecorder.TimingCategory.DATABASE;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Indexes.ascending;

import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.log4j.Logger;
import org.bson.Document;

import com.goodforgoodbusiness.engine.store.container.ContainerStore;
import com.goodforgoodbusiness.model.StorableContainer;
import com.goodforgoodbusiness.model.TriTuple;
import com.goodforgoodbusiness.shared.encode.JSON;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mongodb.ConnectionString;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.ReplaceOptions;

@Singleton
public class MongoContainerStore implements ContainerStore {
	private static final Logger log = Logger.getLogger(MongoContainerStore.class);
	
	private static final String CL_INDEX = "index";
	private static final String CL_CONTAINER = "container";
	
	private final MongoClient client;
	private final ConnectionString connectionString;
	private final MongoDatabase database;
	
	@Inject
	public MongoContainerStore(@Named("containerstore.connectionUrl") String connectionUrl) {
		this.connectionString = new ConnectionString(connectionUrl);
		this.client =  MongoClients.create(connectionString);
		this.database = client.getDatabase(connectionString.getDatabase());
		
		var indexCollection = database.getCollection(CL_INDEX);
		indexCollection.createIndex(ascending("pattern"));

		var containerCollection = database.getCollection(CL_CONTAINER);
		containerCollection.createIndex(ascending("inner_envelope.hashkey"), new IndexOptions().unique(true));
	}
	
	@Override
	public void save(StorableContainer container) {
		log.debug("Put container: " + container.getId());
		
		try (var timer = timer(DATABASE)) {
			// store container as full JSON document
			database
				.getCollection(CL_CONTAINER)
				.replaceOne(
					eq("inner_envelope.hashkey", container.getId()),
					Document.parse(JSON.encodeToString(container)),
					new ReplaceOptions().upsert(true)
				);
			
			// store patterns with all combinations, similar to DHT
			container
				.getTriples()
				.flatMap(triple -> TriTuple.from(triple).matchingCombinations())
				.forEach(tuple -> { 
					database
						.getCollection(CL_INDEX)
						.insertOne(	
							new Document()
								.append("tuple", Document.parse(JSON.encodeToString(tuple)))
								.append("container", container.getId())
						);
				})
			;
		}
	}

	@Override
	public Stream<StorableContainer> searchForPattern(TriTuple tt) {
		try (var timer = timer(DATABASE)) {
			FindIterable<Document> query;
			if (tt.getSubject().isEmpty() && tt.getPredicate().isEmpty() && tt.getObject().isEmpty()) {
				// query for ANY/ANY/ANY
				// these are only allowed in testing but support them in DB layer.
				query = database.getCollection(CL_INDEX).find();
			}
			else {
				// otherwise a normal query with one at least of s/p/o.
				query = 
					database
						.getCollection(CL_INDEX)
						.find(Filters.and(
							// search for tuple with correct signature
							eq("tuple.sub", tt.getSubject().orElse(null)),
							eq("tuple.pre", tt.getPredicate().orElse(null)),
							eq("tuple.obj", tt.getObject().orElse(null))
						))
				;
			}
			
			return 
				StreamSupport.stream(query.spliterator(), true)
				.parallel()
				.map(doc -> doc.getString("container"))
				.map(this::fetch)
				.filter(Optional::isPresent)
				.map(Optional::get)
			;
		}
	}
	
	@Override
	public Optional<StorableContainer> fetch(String id) {
		return 
			Optional.ofNullable(
				database
					.getCollection(CL_CONTAINER)
				 	.find(eq("inner_envelope.hashkey", id))
				 	.first()
			)
			.map(doc -> JSON.decode(doc.toJson(), StorableContainer.class))
		;
	}
	
	protected Stream<StorableContainer> allContainers() {
		return 
			StreamSupport.stream(
				database
					.getCollection(CL_CONTAINER)
				 	.find()
				 	.spliterator(),
				 true
			)
			.map(doc -> JSON.decode(doc.toJson(), StorableContainer.class))
		;
	}
	
	@Override
	public boolean contains(String containerId) {
		long count = database
			.getCollection(CL_CONTAINER)
			.countDocuments(
				eq("inner_envelope.hashkey", containerId)
			);
		
		return count > 0;
	}
}
