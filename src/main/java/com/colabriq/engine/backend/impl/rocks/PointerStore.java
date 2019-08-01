package com.goodforgoodbusiness.engine.backend.impl.rocks;

import java.util.Random;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDBException;

import com.goodforgoodbusiness.engine.backend.Warp;
import com.goodforgoodbusiness.engine.backend.WarpException;
import com.goodforgoodbusiness.engine.backend.Weft;
import com.goodforgoodbusiness.rocks.PrefixIterator;
import com.goodforgoodbusiness.rocks.RocksManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Stores pointers using Rocks, implementing {@link Weft}
 */
@Singleton
public class PointerStore implements Warp {
	private static final Logger log = Logger.getLogger(PointerStore.class);
	
	private static final Random RANDOM = new Random();
	private static final byte [] POINTERS_CFH = "POINTERS".getBytes();
	
	private final RocksManager rocks;
	private final ColumnFamilyHandle cfh;

	@Inject
	public PointerStore(RocksManager rocks) throws RocksDBException {
		this.rocks = rocks;
		this.cfh = rocks.getOrCreateColFH(POINTERS_CFH);
	}
	
	@Override
	public void publishPointer(String pattern, byte[] data) throws WarpException {
		if (log.isDebugEnabled()) {
			log.debug("Publish pointer: " + pattern);
		}
		
		// need to add a random suffix to pattern, because values stored in RocksDB are unique
		
		var patternBytes = pattern.getBytes();
		var key = new byte[patternBytes.length + 16];
		
		RANDOM.nextBytes(key); // fill with random
		System.arraycopy(patternBytes, 0, key, 0, patternBytes.length); // overwrite first part with prefix
		
		try {
			rocks.put(cfh, key, data);
		}
		catch (RocksDBException e) {
			throw new WarpException(e);
		}
	}

	@Override
	public Stream<byte[]> searchForPointers(String pattern) throws WarpException {
		if (log.isDebugEnabled()) {
			log.debug("Search for pointers: " + pattern);
		}
		
		try {
			return new PrefixIterator(rocks.newIterator(cfh), pattern.getBytes())
				.stream()
				.map(row -> row.val)
			;
		}
		catch (RocksDBException e) {
			throw new WarpException(e);
		}
	}
}
