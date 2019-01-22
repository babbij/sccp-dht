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
import com.goodforgoodbusiness.dhtjava.crypto.Crypto;
import com.goodforgoodbusiness.dhtjava.crypto.KeyEncoder;
import com.goodforgoodbusiness.dhtjava.crypto.Symmetric;
import com.goodforgoodbusiness.dhtjava.crypto.abe.ABEException;
import com.goodforgoodbusiness.dhtjava.dht.DHTStore;
import com.goodforgoodbusiness.dhtjava.keys.KeyStore;
import com.goodforgoodbusiness.shared.JSON;
import com.goodforgoodbusiness.shared.model.EncryptedClaim;
import com.goodforgoodbusiness.shared.model.Pointer;
import com.goodforgoodbusiness.shared.model.SubmittableClaim;
import com.google.gson.JsonObject;

import spark.Request;
import spark.Response;
import spark.Route;

public class ClaimsRoute implements Route {
	private static final SecureRandom SRANDOM;
	
	static {
		try {
			SRANDOM = SecureRandom.getInstance("SHA1PRNG");
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Could not load SecureRandom provider", e);
		}
	}
	
	private static final Logger log = Logger.getLogger(ClaimsRoute.class);
	
	private final Crypto crypto;
	private final KeyStore keyStore;
	private final DHTStore dht;
	
	public ClaimsRoute(DHTStore dht, KeyStore keyStore, Crypto crypto) {
		this.dht = dht;
		this.keyStore = keyStore;
		this.crypto = crypto;
	}
	
	@Override
	public Object handle(Request req, Response res) throws Exception {
		log.info("Processing claim post");
		
		var claim = ClaimBuilder.buildFrom(
			JSON.decode(req.body(), SubmittableClaim.class)
		);
		
		log.info("Claim id = " + claim.getId());
		
		// create a new symmetric key for this claim
		var claimKey = Symmetric.generateKey();
		
		EncryptedClaim encryptedClaim = crypto.encryptClaim(claim, claimKey);
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
						crypto.encryptPointer(
							new Pointer(
								encryptedClaim.getId(),
								KeyEncoder.encodeKey(claimKey),
								SRANDOM.nextLong()
							),
							attributes
						)
					);
				}
				catch (ABEException e) {
					throw new RuntimeException(e); // XXX hmmm.
				}


				
//				// record sharekey
//				keyStore.recordKey();
			})
		;
		
		
		
//		  # create share keys for ourself to ensure we can continue to get our own claims
//		  # they can be explicit for each statement.
//		  for pattern in claim.get_patterns(combinations = True, add_time = False, add_mpk = False):
//		    keys.record_key(pattern, abeclient.DEFAULT_ABE.share(pattern))
		
		
		var o = new JsonObject();
		o.addProperty("id", encryptedClaim.getId());
		return o.toString(); 
	}
}
