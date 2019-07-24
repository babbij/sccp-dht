package com.goodforgoodbusiness.engine.backend.impl.rocks;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.log4j.Logger;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDBException;

import com.goodforgoodbusiness.engine.backend.Weft;
import com.goodforgoodbusiness.engine.backend.WeftException;
import com.goodforgoodbusiness.rocks.PrefixIterator;
import com.goodforgoodbusiness.rocks.PrefixIterator.Row;
import com.goodforgoodbusiness.rocks.RocksManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Stores containers using Rocks, implementing {@link Weft}.
 * Just returns local stuff for now.
 * Network fetch layer will be built on top.
 */
@Singleton
public class ContainerStore implements Weft {
	private static final Logger log = Logger.getLogger(ContainerStore.class);
	
	private static final Random RANDOM = new Random();
	
	private static final byte [] LOCATION_CFH = "LOCATIONS".getBytes();
	private static final byte [] DATA_CFH = "DATA".getBytes();
	
	private final RocksManager rocks;
	private final ColumnFamilyHandle locationCFH, dataCFH;

	@Inject
	public ContainerStore(RocksManager rocks) throws RocksDBException {
		this.rocks = rocks;
		this.locationCFH = rocks.getOrCreateColFH(LOCATION_CFH);
		this.dataCFH = rocks.getOrCreateColFH(DATA_CFH);
	}
	
	@Override
	public String publishContainer(String id, byte[] data) throws WeftException {
		if (log.isDebugEnabled()) {
			log.debug("Publish container: " + id);
		}
		
		// create a location for the container.
		// TODO: actually publish there!
		var location = "/localhost/" + UUID.randomUUID();
		
		try {
			// record this location against the id
			// the location needs a suffix because there might be multiple locations for the same ID
			var idBytes = id.getBytes();
			
			var idKey = new byte[idBytes.length + 16];
			RANDOM.nextBytes(idKey);
			System.arraycopy(idBytes, 0, idKey, 0, idBytes.length);
			
			this.rocks.put(locationCFH, idKey, location.getBytes());
			
			// record the container data against location
			this.rocks.put(dataCFH, location.getBytes(), data);
			
			return location;
		}
		catch (RocksDBException e) {
			throw new WeftException(e);
		}
	}

	@Override
	public Stream<String> searchForContainer(String id) throws WeftException {
		if (log.isDebugEnabled()) {
			log.debug("Search for container: " + id);
		}
		
		// look for any known locations for this ID
		// this will return a combination of localhost and remote matches
		
		try {
			var iterator = new PrefixIterator(rocks.newIterator(locationCFH), id.getBytes());
			final Iterable<Row> iterable = () -> iterator;
			
			var stream = StreamSupport.stream(iterable.spliterator(), false);
			stream.onClose(() -> iterator.close());
			
			return stream.map(row -> new String(row.val));
		}
		catch (RocksDBException e) {
			throw new WeftException(e);
		}
	}

	@Override
	public Optional<byte[]> fetchContainer(String location) throws WeftException {
		if (log.isDebugEnabled()) {
			log.debug("Fetch container: " + location);
		}
		
		try {
			return Optional.ofNullable(rocks.get(dataCFH, location.getBytes()));
		}
		catch (RocksDBException e) {
			throw new WeftException(e);
		}
	}
}
