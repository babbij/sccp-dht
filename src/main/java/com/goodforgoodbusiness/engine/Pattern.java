package com.goodforgoodbusiness.engine;

import static com.goodforgoodbusiness.shared.TripleUtil.toValueArray;
import static com.goodforgoodbusiness.shared.encode.Hash.sha256;
import static java.util.stream.Collectors.toSet;
import static org.apache.jena.graph.Node.ANY;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.engine.store.keys.spec.ShareKeySpec;
import com.goodforgoodbusiness.shared.encode.CBOR;
import com.goodforgoodbusiness.shared.encode.Hex;

public class Pattern {
	private static final String PREFIX = "a";
	
	/**
	 * Return the pattern to search the network for a particular Triple
	 */
	public static String forSearch(Triple pattern) throws PatternException {
		try {
			// prefixing 'a' here makes all signatures compatible
			// with OpenABE which doesn't like attributes beginning with digits
			//it also gives us an opportunity to version the hash!		
			return PREFIX + Hex.encode(sha256(CBOR.forObject(toValueArray(pattern))));
		}
		catch (Exception e) {
			throw new PatternException("Pointer creation error", e);
		}
	}
	
	/**
	 * Return the pattern for a particular share key specification
	 */
	public static String forSpec(ShareKeySpec spec) {
		try {
			return PREFIX + Hex.encode(sha256(CBOR.forObject(
				new String [] { spec.getSubject(), spec.getPredicate(), spec.getObject() }
			)));
		}
		catch (Exception e) {
			throw new PatternException("Pointer creation error", e);
		}
	}
	
	/**
	 * Return patterns to publish a triple under so it can be found by
	 * various searches.
	 */
	public static Set<String> forPublish(Triple triple) {

		
		// generate possible pointers
		return combinations(triple)
			.stream()
			.map(Pattern::forSearch)
			.collect(Collectors.toSet())
		;
	}
	
	public static Set<Triple> combinations(Triple triple) {
		var sub = triple.getSubject();
		var pre = triple.getPredicate();
		var obj = triple.getObject();
		
		return 
			Stream.of(
				triple, 
				new Triple(sub, pre, ANY),
				new Triple(sub, ANY, obj),
				new Triple(ANY, pre, obj),
				new Triple(sub, ANY, ANY),
				new Triple(ANY, ANY, obj)
			)
			// remove (ANY, ANY, ANY) and (ANY, pre, ANY)
			// these can crop up if you specified an incomplete Triple
			.filter(t -> !(t.getSubject().equals(ANY) && t.getObject().equals(ANY)))
			.collect(toSet())
		;
	}
}
