package com.goodforgoodbusiness.engine.route;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import spark.Request;
import spark.Response;
import spark.Route;

@Singleton
public class PingRoute implements Route {
	@Inject
	public PingRoute() {
	}
	
	@Override
	public Object handle(Request req, Response res) throws Exception {
		return "OK";
	}
}
