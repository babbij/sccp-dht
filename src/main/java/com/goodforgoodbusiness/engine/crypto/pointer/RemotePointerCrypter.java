package com.goodforgoodbusiness.engine.crypto.pointer;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.engine.crypto.KeyManager;
import com.goodforgoodbusiness.engine.store.keys.ShareKeyStore;
import com.goodforgoodbusiness.kpabe.KPABEException;
import com.goodforgoodbusiness.kpabe.local.KPABELocalInstance;
import com.goodforgoodbusiness.kpabe.remote.KPABERemote;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Remote pointer encrypt/decrypt and maintains the keystore.
 * EXPERIMENT.
 */
public class RemotePointerCrypter extends PointerCrypter {
	private static final Logger log = Logger.getLogger(PointerCrypter.class);
	
	private final KeyManager keyManager;
	private final String [] remotes;
	private final AtomicInteger counter = new AtomicInteger(0);
	
	@Inject
	public RemotePointerCrypter(KeyManager keyManager, ShareKeyStore store, @Named("kpabe.remotes") String remotes) {
		super(store);
		this.keyManager = keyManager;
		this.remotes = Stream.of(remotes.trim().split(",")).map(String::trim).toArray(i -> new String[i]);
	}
	
	private String chooseNextRemote() {
		return remotes[counter.getAndIncrement() % remotes.length];
	}
	
	@Override
	protected String encrypt(String data, String attributes) throws KPABEException, InvalidKeyException {
		try {
			var remote = (KPABERemote)Naming.lookup(chooseNextRemote());
			var instance = remote.forKeys(keyManager.getPublicKey(), keyManager.getSecretKey());
			return instance.encrypt(data, attributes);
		}
		catch (RemoteException | MalformedURLException | NotBoundException e) {
			log.error("Could not reach remote kpabe", e);
			return KPABELocalInstance.forKeys(keyManager.getPublicKey(), keyManager.getSecretKey()).encrypt(data, attributes);
		}
	}

	@Override
	protected KeyPair shareKey(String pattern) throws KPABEException, InvalidKeyException {
		try {
			var remote = (KPABERemote)Naming.lookup(chooseNextRemote());
			var instance = remote.forKeys(keyManager.getPublicKey(), keyManager.getSecretKey());
			return instance.shareKey(pattern);
		}
		catch (RemoteException | MalformedURLException | NotBoundException e) {
			log.error("Could not reach remote kpabe", e);
			return KPABELocalInstance.forKeys(keyManager.getPublicKey(), keyManager.getSecretKey()).shareKey(pattern);
		}
	}

	@Override
	protected String decrypt(String data, KeyPair keyPair) throws KPABEException, InvalidKeyException {
		try {
			var remote = (KPABERemote)Naming.lookup(chooseNextRemote());
			return remote.decrypt(data, keyPair);
		}
		catch (RemoteException | MalformedURLException | NotBoundException e) {
			log.error("Could not reach remote kpabe", e);
			return KPABELocalInstance.decrypt(data, keyPair);
		}
	}
}
