package com.goodforgoodbusiness.engine.store.keys.impl;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.or;
import static java.util.stream.StreamSupport.stream;

import java.util.LinkedList;
import java.util.stream.Stream;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.goodforgoodbusiness.engine.crypto.primitive.key.EncodeableShareKey;
import com.goodforgoodbusiness.engine.store.keys.ShareKeyStore;
import com.goodforgoodbusiness.kpabe.key.KPABEPublicKey;
import com.goodforgoodbusiness.model.TriTuple;
import com.goodforgoodbusiness.shared.encode.JSON;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

@Singleton
public class MongoKeyStore implements ShareKeyStore {
	private static final String CL_KNOWN = "known";
	private static final String CL_KEYS = "keys";
	
	private final MongoClient client;
	private final ConnectionString connectionString;
	private final MongoDatabase database;
	
	@Inject
	public MongoKeyStore(@Named("keystore.connectionUrl") String connectionUrl) {
		this.connectionString = new ConnectionString(connectionUrl);
		this.client =  MongoClients.create(connectionString);
		this.database = client.getDatabase(connectionString.getDatabase());
	}
	
	@Override
	public void saveKey(TriTuple tuple, EncodeableShareKey shareKey) {
		database
			.getCollection(CL_KNOWN)
			.insertOne(
				new Document()
					.append("pattern", Document.parse(JSON.encodeToString(tuple)))
					.append("public", shareKey.getPublic().toString())
			)
		;
		
		database
			.getCollection(CL_KEYS)
			.insertOne(
				Document.parse(JSON.encodeToString(shareKey))
			)
		;
	}
	
	@Override
	public Stream<KPABEPublicKey> knownSharers(TriTuple pattern) {
		var filters = new LinkedList<Bson>();
		
		if (pattern.getSubject().isPresent()) {
			filters.add(eq("pattern.sub", pattern.getSubject().get()));
		}
		
		if (pattern.getPredicate().isPresent()) {
			filters.add(eq("pattern.pre", pattern.getPredicate().get()));
		}
		
		if (pattern.getObject().isPresent()) {
			filters.add(eq("pattern.obj", pattern.getObject().get()));
		}
		
		return 
			stream(
				database.getCollection(CL_KNOWN)
					.find(or(filters))
					.spliterator(),
				true
			)
			.map(storedPublicKey -> 
				new KPABEPublicKey(storedPublicKey.getString("public"))
			)
		;
	}
	
	@Override
	public Stream<EncodeableShareKey> keysForDecrypt(KPABEPublicKey publicKey) {
		return 
			stream(
				database.getCollection(CL_KEYS)
					.find(eq("public", publicKey.toString()))
					.spliterator(),
				true
			)
			.map(storedShareKey -> 
				JSON.decode(storedShareKey.toJson(), EncodeableShareKey.class))
		;
		
	}

	/**
	 * DANGER DANGER, testing only.
	 */
	public void clearAll() {
		database.getCollection(CL_KNOWN).deleteMany(new BasicDBObject());
		database.getCollection(CL_KEYS).deleteMany(new BasicDBObject());
	}
}

