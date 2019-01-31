package com.goodforgoodbusiness.engine.dht.impl.remote;

import static com.goodforgoodbusiness.dht.RosterUtil.ROSTER_NAME;
import static com.goodforgoodbusiness.dht.RosterUtil.lookup;
import static com.goodforgoodbusiness.dht.RosterUtil.lookupParticipant;
import static java.rmi.registry.LocateRegistry.createRegistry;
import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.dht.Participant;
import com.goodforgoodbusiness.dht.Registration;
import com.goodforgoodbusiness.dht.Roster;
import com.goodforgoodbusiness.dht.RosterException;

public class RosterClient<T extends Participant> {
	private static final Logger log = Logger.getLogger(RosterClient.class);
	
	private static int port() {
		return 9000 + new Random().nextInt(999);
	}
	
	private final Class<T> clazz;
	
	private final int port;
	
	private final String rosterUrl;
	private final String endpoint;
	
	private final Registry registry;
	private final ScheduledExecutorService scheduler = newScheduledThreadPool(1);
	
	public RosterClient(String rosterUrl, Class<T> clazz, T obj) throws RosterException {
		this.clazz = clazz;
		this.port = port();
		
		try {
			this.rosterUrl = rosterUrl + (rosterUrl.endsWith("/") ? "" : "/") + ROSTER_NAME;
			this.endpoint = "//" + InetAddress.getLocalHost().getHostName() + ":" + port + "/object";
		}
		catch (UnknownHostException e) {
			throw new RosterException("Could not initialize client", e);
		}
		
		log.info("Creating registry at " + endpoint + " and binding self");
		
		try {
			this.registry = createRegistry(port);
			this.registry.bind("object", obj);
		}
		catch (RemoteException | AlreadyBoundException e) {
			throw new RosterException("Could not initialize client", e);
		}
		
		scheduler.scheduleAtFixedRate(
			() -> {
				try {
					// self register
					lookup(this.rosterUrl, Roster.class)
						.orElseThrow(() -> new RosterException("Unable to find roster at " + rosterUrl))
						.register(endpoint);
					;
				}
				catch (RemoteException | RosterException e) {
					log.error("Could not bind at this time: " +  e.getMessage());
				}
			}, 
			0, 
			15, 
			SECONDS
		);
	}
	
	private Optional<Roster> getRoster() {
		return lookup(rosterUrl, Roster.class);
	}
	
	public Stream<Registration<T>> getRegistrations() throws RosterException {
		return getRoster()
			.map(roster -> {
				try {
					return roster.getRegistrations()
						.stream()
						.flatMap(location -> lookupParticipant(location, clazz).stream())
					;
				}
				catch (RemoteException | RosterException e) {
					return Stream.<Registration<T>>empty();
				}
			})
			.orElse(Stream.empty())
		;
	}
	
	public Optional<Registration<T>> lookupByLocation(String location) {
		return lookup(location, clazz)
			.map(remote -> new Registration<T>(location, remote));
	}
}
