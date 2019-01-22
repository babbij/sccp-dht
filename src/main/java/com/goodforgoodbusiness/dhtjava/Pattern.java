package com.goodforgoodbusiness.dhtjava;

import static com.goodforgoodbusiness.shared.EncodeUtil.cborDigestString;
import static org.apache.jena.graph.Node.ANY;

import java.util.stream.Stream;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.shared.TripleUtil;

public class Pattern {
	private static final String HASH_PREFIX = "a";
	
	/**
	 * Return the pattern to search the network for a particular Triple
	 */
	public static String forSearch(Triple pattern) throws PatternException {
		try {
			// prefixing 'a' here makes all signatures compatible
			// with OpenABE which doesn't like attributes beginning with digits
			//it also gives us an opportunity to version the hash!		
			return HASH_PREFIX + cborDigestString( TripleUtil.toValueArray(pattern) );
		}
		catch (Exception e) {
			throw new PatternException("Pointer creation error", e);
		}
	}
	
	/**
	 * Return patterns to publish a triple under so it can be found by
	 * various searches.
	 */
	public static Stream<String> forPublish(Triple triple) {
		var sub = triple.getSubject();
		var pre = triple.getPredicate();
		var obj = triple.getObject();
		
		// generate possible pointers
		return Stream
			.of(
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
			.map(Pattern::forSearch)
		;
	}
}
