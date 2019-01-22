package com.goodforgoodbusiness.dhtjava.keys.impl;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.or;
import static com.mongodb.client.model.Indexes.ascending;
import static java.util.stream.StreamSupport.stream;

import java.util.LinkedList;
import java.util.Objects;
import java.util.stream.Stream;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.goodforgoodbusiness.dhtjava.keys.KeyIndex;
import com.goodforgoodbusiness.dhtjava.keys.KeyStore;
import com.goodforgoodbusiness.dhtjava.keys.StoredKey;
import com.goodforgoodbusiness.shared.JSON;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;

public class MongoKeyStore extends KeyStore {
	private static final String CL_KEYS = "keys";
	
	private final MongoClient client;
	private final ConnectionString connectionString;
	private final MongoDatabase database;
	
	public MongoKeyStore(String connectionUrl) {
		this.connectionString = new ConnectionString(connectionUrl);
		this.client =  MongoClients.create(connectionString);
		this.database = client.getDatabase(connectionString.getDatabase());
		
		var keyCollection = database.getCollection(CL_KEYS);
		
		keyCollection.createIndex(ascending("subject"));
		keyCollection.createIndex(ascending("predicate"));
		keyCollection.createIndex(ascending("object"));
		
		keyCollection.createIndex(ascending("key"), new IndexOptions().unique(true));
	}
	
	@Override
	public void saveKey(KeyIndex idx, StoredKey storedKey) {
		database
			.getCollection(CL_KEYS)
			.insertOne(
				Document
					.parse(JSON.encodeToString(idx))
					.append("key", Document.parse(JSON.encodeToString(storedKey)))
			)
		;
	}

	@Override
	public Stream<StoredKey> findKey(KeyIndex idx) {
		var filters = new LinkedList<Bson>();
		
		if (idx.getSubject() != null) {
			filters.add(eq("subject", idx.getSubject()));
		}
		
		if (idx.getPredicate() != null) {
			filters.add(eq("predicate", idx.getPredicate()));
		}
		
		if (idx.getObject() != null) {
			filters.add(eq("object", idx.getObject()));
		}
		
		return 
			stream(
				database
					.getCollection(CL_KEYS)
					.find(or(filters))
					.spliterator(),
				true
			)
			.map(doc -> JSON.decode( ((Document)doc.get("key")).toJson() , StoredKey.class))
			.filter(Objects::nonNull)
		;
	}
}
