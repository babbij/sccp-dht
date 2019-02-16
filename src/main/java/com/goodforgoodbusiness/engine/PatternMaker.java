package com.goodforgoodbusiness.engine;

import java.util.stream.Stream;

import com.goodforgoodbusiness.engine.crypto.KeyManager;
import com.goodforgoodbusiness.kpabe.key.KPABEPublicKey;
import com.goodforgoodbusiness.model.TriTuple;
import com.goodforgoodbusiness.shared.Rounds;
import com.goodforgoodbusiness.shared.encode.CBOR;
import com.goodforgoodbusiness.shared.encode.Hash;
import com.goodforgoodbusiness.shared.encode.Hex;

/**
 * Build index patterns for publishing in to the DHT.
 */
public final class PatternMaker {
	private static String hash(KPABEPublicKey key, TriTuple tt) {
		var cbor = CBOR.forObject(new Object [] { 
			key.toString(),
			
			tt.getSubject().orElse(null),
			tt.getPredicate().orElse(null),
			tt.getObject().orElse(null) 
		});
		
		return Hex.encode(Rounds.apply(Hash::sha512, cbor, 2)); // two rounds
	}
	
	/**
	 * Generate all possible pattern hashes.
	 * These also include the public key of the publishing party.
	 */
	public static Stream<String> forPublish(KeyManager keyManager, TriTuple tuple) {
		return tuple
			.matchingCombinations()
			// for DHT publish, tuple pattern must have either defined subject or defined object
			.filter(tt -> tt.getSubject().isPresent() || tt.getObject().isPresent())
			.map(tt -> hash(keyManager.getPublicKey(), tt))
			.parallel()
		;
	}
	
	/** 
	 * Generate the pattern hash for this specific TriTuple only.
	 */
	public static String forSearch(KPABEPublicKey key, TriTuple tuple) {
		return hash(key, tuple);
	};
	
	private PatternMaker() {
	}
}
