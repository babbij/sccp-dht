package com.goodforgoodbusiness.engine.route;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.engine.dht.DHTSearcher;
import com.goodforgoodbusiness.shared.encode.JSON;
import com.goodforgoodbusiness.webapp.ContentType;
import com.goodforgoodbusiness.webapp.error.BadRequestException;
import com.google.inject.Inject;

import spark.Request;
import spark.Response;
import spark.Route;

public class MatchesRoute implements Route {
	private static final Logger log = Logger.getLogger(MatchesRoute.class);
	
	private final DHTSearcher searcher;
	
	@Inject
	public MatchesRoute(DHTSearcher searcher) {
		this.searcher = searcher;
	}
	
	@Override
	public Object handle(Request req, Response res) throws Exception {
		res.type(ContentType.json.getContentTypeString());
		
		var triple = JSON.decode(req.queryParams("pattern"), Triple.class);
		
		if (triple != null) {
			log.info("Matches called for " + triple);
			
			if ((triple.getSubject() == Node.ANY) && (triple.getObject() == Node.ANY)) {
				throw new BadRequestException("Searching DHT for (?, _, ?) or (_, _ , _) not supported");
			}
			
			// return found results
			return JSON.encode(searcher.search(triple));
		}
		else {
			throw new BadRequestException("Must specify a triple");
		}
	}
}
