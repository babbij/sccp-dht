package com.goodforgoodbusiness.engine.route;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.engine.ContainerBuilder;
import com.goodforgoodbusiness.engine.Publisher;
import com.goodforgoodbusiness.engine.store.container.ContainerStore;
import com.goodforgoodbusiness.model.SubmittableContainer;
import com.goodforgoodbusiness.shared.encode.JSON;
import com.goodforgoodbusiness.webapp.ContentType;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import spark.Request;
import spark.Response;
import spark.Route;

@Singleton
public class ContainerSubmitRoute implements Route {	
	private static final Logger log = Logger.getLogger(ContainerSubmitRoute.class);
	
	private final ContainerBuilder builder;
	private final ContainerStore store;
	private final Publisher publisher;
	
	@Inject
	public ContainerSubmitRoute(ContainerBuilder builder, ContainerStore store, Publisher publisher) {
		this.builder = builder;
		this.store = store;
		this.publisher = publisher;
	}
	
	@Override
	public Object handle(Request req, Response res) throws Exception {
		res.type(ContentType.json.getContentTypeString());
		
		var container = builder.buildFrom(JSON.decode(req.body(), SubmittableContainer.class));
		log.info("Processing posted container: " + container);
		
		// store locally + push to DHt
		store.save(container);
		publisher.publish(container);
		
		// result is { "id" : <hash> }
		var o = new JsonObject();
		o.addProperty("id", container.getId());
		return o.toString(); 
	}
}
