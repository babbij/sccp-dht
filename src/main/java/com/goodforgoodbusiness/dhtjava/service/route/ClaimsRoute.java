package com.goodforgoodbusiness.dhtjava.service.route;

import static com.google.common.collect.Streams.concat;
import static java.lang.System.currentTimeMillis;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.dhtjava.ClaimBuilder;
import com.goodforgoodbusiness.dhtjava.Pattern;
import com.goodforgoodbusiness.dhtjava.crypto.ClaimCrypter;
import com.goodforgoodbusiness.dhtjava.crypto.PointerCrypter;
import com.goodforgoodbusiness.dhtjava.dht.DHTStore;
import com.goodforgoodbusiness.kpabe.KPABEException;
import com.goodforgoodbusiness.shared.ContentType;
import com.goodforgoodbusiness.shared.JSON;
import com.goodforgoodbusiness.shared.model.Pointer;
import com.goodforgoodbusiness.shared.model.SubmittableClaim;
import com.google.gson.JsonObject;
import com.google.inject.Inject;

import spark.Request;
import spark.Response;
import spark.Route;

public class ClaimsRoute implements Route {	
	private static final Logger log = Logger.getLogger(ClaimsRoute.class);
	
	private static final SecureRandom RANDOM;
	
	static {
		try {
			RANDOM = SecureRandom.getInstanceStrong();
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
	private final ClaimBuilder builder;
	private final PointerCrypter pointerCrypter;
	private final DHTStore dht;
	
	@Inject
	public ClaimsRoute(ClaimBuilder builder, DHTStore dht, PointerCrypter pointerCrypto) {
		this.builder = builder;
		this.dht = dht;
		this.pointerCrypter = pointerCrypto;
	}
	
	@Override
	public Object handle(Request req, Response res) throws Exception {
		res.type(ContentType.json.getContentTypeString());
		
		log.info("Processing claim post");
		
		var claim = builder.buildFrom(
			JSON.decode(req.body(), SubmittableClaim.class)
		);
		
		log.info("Claim id = " + claim.getId());
		
		var crypter = new ClaimCrypter(); // with new symmetric key for claim
		var encryptedClaim = crypter.encrypt(claim);
		
		dht.putClaim(encryptedClaim);
		
		// generate combinations for pointers
		var patterns = claim
			.getTriples()
			.flatMap(Pattern::forPublish)
			.collect(toSet())
		;
		
		// the attributes to use for encryption are patterns + timestamp
		// so that keys can be issued with a pattern + a timestamp range to
		// restrict access to a particular temporal window.
		var attributes = concat(
			patterns.stream(),
			of(Long.toString(currentTimeMillis() / 1000L))
		);
		
		// create a pointer for each pattern
		// the pointer needs to be encrypted with _all_ the possible patterns
		patterns
			.forEach(pattern -> {
				log.info("Putting pattern " + pattern);
				
				try {
					dht.putPointer(
						pattern,
						pointerCrypter.encrypt(
							new Pointer(
								encryptedClaim.getId(),
								crypter.getSecretKey().toEncodedString(),
								RANDOM.nextLong()
							),
							attributes
						)
					);
				}
				catch (KPABEException e) {
					throw new RuntimeException(e); // XXX hmmm.
				}
	
//				  # create share keys for ourself to ensure we can continue to get our own claims
//				  # they can be explicit for each statement.
//				  for pattern in claim.get_patterns(combinations = True, add_time = False, add_mpk = False):
//				    keys.record_key(pattern, abeclient.DEFAULT_ABE.share(pattern))
			})
		;
		
		var o = new JsonObject();
		o.addProperty("id", encryptedClaim.getId());
		return o.toString(); 
	}
}
