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
import com.google.inject.Singleton;

import spark.Request;
import spark.Response;
import spark.Route;

@Singleton
public class ClaimSubmitRoute implements Route {	
	private static final Logger log = Logger.getLogger(ClaimSubmitRoute.class);
	
	private final ClaimBuilder builder;
	private final ClaimStore store;
	private final DHTPublisher publisher;
	
	@Inject
	public ClaimSubmitRoute(ClaimBuilder builder, ClaimStore store, DHTPublisher publisher) {
		this.builder = builder;
		this.store = store;
		this.publisher = publisher;
	}
	
	@Override
	public Object handle(Request req, Response res) throws Exception {
		res.type(ContentType.json.getContentTypeString());
		
		var claim = builder.buildFrom(JSON.decode(req.body(), SubmittableClaim.class));
		log.info("Processing posted claim: " + claim);
		
		// store locally + push to DHt
		store.save(claim);
		publisher.publishClaim(claim);
		
		// result is { "id" : <hash> }
		var o = new JsonObject();
		o.addProperty("id", claim.getId());
		return o.toString(); 
	}
}
