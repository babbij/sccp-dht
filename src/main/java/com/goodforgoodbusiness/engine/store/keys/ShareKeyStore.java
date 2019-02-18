package com.goodforgoodbusiness.engine.store.keys;

import java.util.stream.Stream;

import com.goodforgoodbusiness.engine.crypto.key.EncodeableShareKey;
import com.goodforgoodbusiness.kpabe.key.KPABEPublicKey;
import com.goodforgoodbusiness.model.TriTuple;

public interface ShareKeyStore {
	/**
	 * Find any public key identities who shared something with us
	 */
	public Stream<KPABEPublicKey> knownContainerCreators(TriTuple tuple);
	
	/**
	 * Retrieve all keys shared with us by a particular MPK (public key).
	 */
	public Stream<EncodeableShareKey> keysForDecrypt(KPABEPublicKey publicKey, TriTuple tuple);

	/**
	 * Save a key for future retrieval via the find... methods
	 */
	public void saveKey(TriTuple tuple, EncodeableShareKey key);
}
