package com.goodforgoodbusiness.dhtjava.service;

import java.io.IOException;

import com.goodforgoodbusiness.dhtjava.ClaimBuilder;
import com.goodforgoodbusiness.dhtjava.crypto.PointerCrypter;
import com.goodforgoodbusiness.dhtjava.dht.DHTStore;
import com.goodforgoodbusiness.dhtjava.service.route.ClaimsRoute;
import com.goodforgoodbusiness.dhtjava.service.route.MatchesRoute;
import com.goodforgoodbusiness.shared.web.cors.CorsFilter;
import com.goodforgoodbusiness.shared.web.cors.CorsRoute;
import com.goodforgoodbusiness.shared.web.error.BadRequestException;
import com.goodforgoodbusiness.shared.web.error.BadRequestExceptionHandler;
import com.goodforgoodbusiness.shared.web.error.IOExceptionHandler;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import spark.Service;

@Singleton
public class DHTService {
	private final int port;
	
	private final ClaimBuilder claimBuilder;
	private final DHTStore dht;
	private final PointerCrypter pointerCrypter;
	
	protected Service service = null;
	
	@Inject
	public DHTService(@Named("port") int port, ClaimBuilder claimBuilder, DHTStore dht, PointerCrypter pointerCrypter) {
		this.port = port;
		
		this.claimBuilder = claimBuilder;
		this.dht = dht;
		this.pointerCrypter = pointerCrypter;
	}
	
	protected void configure() {
		service.options("/*", new CorsRoute());
		service.before(new CorsFilter());
		
		service.get("/matches", new MatchesRoute(dht, pointerCrypter));
		service.post("/claims", new ClaimsRoute(claimBuilder, dht, pointerCrypter));
		
		service.exception(BadRequestException.class, new BadRequestExceptionHandler());
		service.exception(IOException.class, new IOExceptionHandler());
	}
	
	public final void start() {
		service = Service.ignite();
		service.port(port);
		
		configure();
		
		service.awaitInitialization();
	}
}
