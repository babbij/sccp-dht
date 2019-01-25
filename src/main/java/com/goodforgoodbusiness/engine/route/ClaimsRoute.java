package com.goodforgoodbusiness.engine.route;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.engine.ClaimBuilder;
import com.goodforgoodbusiness.engine.dht.DHTPublisher;
import com.goodforgoodbusiness.engine.store.claim.ClaimStore;
import com.goodforgoodbusiness.model.SubmittableClaim;
import com.goodforgoodbusiness.shared.encode.JSON;
import com.goodforgoodbusiness.webapp.ContentType;
import com.google.gson.JsonObject;
import com.google.inject.Inject;

import spark.Request;
import spark.Response;
import spark.Route;

public class ClaimsRoute implements Route {	
	private static final Logger log = Logger.getLogger(ClaimsRoute.class);
	
	private final ClaimBuilder builder;
	private final ClaimStore store;
	private final DHTPublisher publisher;
	
	@Inject
	public ClaimsRoute(ClaimBuilder builder, ClaimStore store, DHTPublisher publisher) {
		this.builder = builder;
		this.store = store;
		this.publisher = publisher;
	}
	
	@Override
	public Object handle(Request req, Response res) throws Exception {
		res.type(ContentType.json.getContentTypeString());
		log.info("Processing posted claim");
		
		// build claim + store a copy of it locally
		var claim = builder.buildFrom(JSON.decode(req.body(), SubmittableClaim.class));
		store.save(claim);
		
		// now on to DHT
		publisher.publishClaim(claim);
		
		// result is { "id" : <hash> }
		var o = new JsonObject();
		o.addProperty("id", claim.getId());
		return o.toString(); 
	}
}
