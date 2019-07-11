package com.goodforgoodbusiness.engine.route;

import static com.goodforgoodbusiness.shared.TimingRecorder.timer;
import static com.goodforgoodbusiness.shared.TimingRecorder.TimingCategory.DHT_ROUTE_SEARCH;
import static java.util.stream.Collectors.toSet;

import java.util.Set;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.engine.Searcher;
import com.goodforgoodbusiness.engine.store.container.ContainerStore;
import com.goodforgoodbusiness.model.StorableContainer;
import com.goodforgoodbusiness.model.TriTuple;
import com.goodforgoodbusiness.shared.encode.JSON;
import com.goodforgoodbusiness.webapp.ContentType;
import com.goodforgoodbusiness.webapp.error.BadRequestException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import spark.Request;
import spark.Response;
import spark.Route;

@Singleton
public class MatchSearchRoute implements Route {
	private static final Logger log = Logger.getLogger(MatchSearchRoute.class);
	
	private final ContainerStore store;
	private final Searcher searcher;

	private final boolean allowTestQueries;
	
	@Inject
	public MatchSearchRoute(ContainerStore store, Searcher searcher, @Named("allow.test.queries") boolean allowTestQueries) {
		this.store = store;
		this.searcher = searcher;
		
		this.allowTestQueries = allowTestQueries;
	}
	
	@Override
	public Object handle(Request req, Response res) throws Exception {
		var tuple = JSON.decode(req.queryParams("pattern"), TriTuple.class);
		if (tuple != null) {
			log.debug("Matches called for " + tuple);
			
			if (!tuple.getSubject().isPresent() && !tuple.getObject().isPresent()) {
				var x = 1;
				System.out.println(x);
			}
			
			if (!allowTestQueries) {
				if (!tuple.getSubject().isPresent() && !tuple.getObject().isPresent()) {
					log.warn("This combination of s/p/o is not permitted");
					throw new BadRequestException("Searching DHT for (?, _, ?) or (_, _ , _) not supported");
				}
			}
			
			Set<StorableContainer> resultContainers;
			
			try (var timer = timer(DHT_ROUTE_SEARCH)) {
				// search for remote containers, store in local store
				var newContainers = searcher.search(tuple, true).collect(toSet());
				if (log.isDebugEnabled()) log.debug("new = " + newContainers);
				
				// retrieve all containers from local store
				// includes those just fetched and others we already knew.
				resultContainers = store.searchForPattern(tuple).collect(toSet());
				if (log.isDebugEnabled()) log.debug("known = " + resultContainers);
			}
			
			// return known result containers
			return JSON.encode(resultContainers);
		}
		else {
			throw new BadRequestException("Must specify a triple");
		}
	}
}
