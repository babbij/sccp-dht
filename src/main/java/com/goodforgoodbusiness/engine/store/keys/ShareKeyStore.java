package com.goodforgoodbusiness.engine.store.keys;

import java.util.stream.Stream;

import com.goodforgoodbusiness.engine.crypto.primitive.key.EncodeableShareKey;
import com.goodforgoodbusiness.kpabe.key.KPABEPublicKey;
import com.goodforgoodbusiness.model.TriTuple;

public interface ShareKeyStore {
	/**
	 * Find any public key identities who shared something with us
	 */
	public Stream<KPABEPublicKey> knownSharers(TriTuple tuple);
	
	/**
	 * Retrieve all keys shared with us by a particular MPK (public key).
	 */
	public Stream<EncodeableShareKey> keysForDecrypt(KPABEPublicKey publicKey);

	/**
	 * Save a key for future retrieval via the find... methods
	 */
	public void saveKey(TriTuple tuple, EncodeableShareKey key);
}
