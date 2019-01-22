package com.goodforgoodbusiness.dhtjava.service.route;

import static com.goodforgoodbusiness.dhtjava.crypto.KeyEncoder.decodeSecretKey;

import java.security.InvalidKeyException;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.dhtjava.DHTApp;
import com.goodforgoodbusiness.dhtjava.Pattern;
import com.goodforgoodbusiness.dhtjava.crypto.Crypto;
import com.goodforgoodbusiness.dhtjava.crypto.CryptoException;
import com.goodforgoodbusiness.dhtjava.dht.DHTStore;
import com.goodforgoodbusiness.dhtjava.dht.share.ShareKeyStore;
import com.goodforgoodbusiness.kpabe.KPABEException;
import com.goodforgoodbusiness.shared.JSON;
import com.goodforgoodbusiness.shared.model.Pointer;
import com.goodforgoodbusiness.shared.model.StoredClaim;
import com.goodforgoodbusiness.shared.web.error.BadRequestException;
import com.google.inject.Inject;

import spark.Request;
import spark.Response;
import spark.Route;

public class MatchesRoute implements Route {
	private static final Logger log = Logger.getLogger(MatchesRoute.class);
	
	private final Crypto crypto;
	private final ShareKeyStore keyStore;
	private final DHTStore dht;
	
	@Inject
	public MatchesRoute(DHTStore dht, ShareKeyStore keyStore, Crypto crypto) {
		this.dht = dht;
		this.keyStore = keyStore;
		this.crypto = crypto;
	}
	
	@Override
	public Object handle(Request req, Response res) throws Exception {
		var triple = JSON.decode(req.queryParams("pattern"), Triple.class);
		log.info("Matches called for " + triple);
		
		if ((triple.getSubject() == Node.ANY) && (triple.getObject() == Node.ANY)) {
			throw new BadRequestException("Searching DHT for (?, _, ?) or (_, _ , _) not supported");
		}
		
		var pattern = Pattern.forSearch(triple);
		
		var claims =
			// pointer -> encrypted claim -> stored claim
			// check for nulls at each stage (not errors)
			dht.getPointers(pattern)
				.map(data -> decryptPointer(pattern, data))
				.filter(Objects::nonNull)
				.map(pointer -> fetchClaim(pointer))
				.filter(Objects::nonNull)
				.collect(Collectors.toSet())
			;

		return JSON.encode(claims);
	}
	
	private Pointer decryptPointer(String pattern, String data) {			
		log.info("Decrypting pointer: " + data.substring(0, 10) + "...");
		
		try {
			// XXX create temporary key
			// this key should be able to decrypt the pointer
			var shareKey = DHTApp.kpabe.shareKey(pattern); 
			
			var result = crypto.decryptPointer(data, shareKey);
			if (result != null) {
				return result;
			}
			else {
				log.info("Decryption failed");
				return null;
			}
		}
		catch (InvalidKeyException | KPABEException e) {
			// unsuccessful decryption returns null
			// if an Exception is thrown this is a legitimate error.
			log.error("Error decrypting pointer", e);
			return null;
		}
	}
	
	private StoredClaim fetchClaim(Pointer pointer) {
		log.info("Fetching claim: " + pointer.getClaimId());
		var claim = dht.getClaim(pointer.getClaimId());
		
		if (claim != null) {
			try {
				log.info("Decrypting claim: " + claim.getId());
				return crypto.decryptClaim(claim, decodeSecretKey(pointer.getClaimKey()));
			}
			catch (CryptoException e) {
				log.error("Error decrypting claim", e); 
			}
		}
		
		return null;
	}
}
