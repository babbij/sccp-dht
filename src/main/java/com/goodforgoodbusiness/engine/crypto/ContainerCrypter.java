package com.goodforgoodbusiness.engine.crypto;

import com.goodforgoodbusiness.engine.crypto.primitive.EncryptionException;
import com.goodforgoodbusiness.engine.crypto.primitive.SymmetricEncryption;
import com.goodforgoodbusiness.engine.crypto.primitive.key.EncodeableSecretKey;
import com.goodforgoodbusiness.model.Contents;
import com.goodforgoodbusiness.model.EncryptedContainer;
import com.goodforgoodbusiness.model.EncryptedEnvelope;
import com.goodforgoodbusiness.model.Envelope;
import com.goodforgoodbusiness.model.StoredContainer;
import com.goodforgoodbusiness.shared.encode.JSON;

public class ContainerCrypter {
	private final EncodeableSecretKey secretKey;
	
	/**
	 * Create brand new key
	 */
	public ContainerCrypter() {
		this.secretKey = SymmetricEncryption.createKey();
	}
	
	/**
	 * Create using existing key
	 */
	public ContainerCrypter(String encodedForm) {
		this.secretKey = new EncodeableSecretKey(encodedForm);
	}
	
	public EncodeableSecretKey getSecretKey() {
		return secretKey;
	}

	public EncryptedContainer encrypt(StoredContainer container) throws EncryptionException {
		var contents = JSON.encodeToString(container.getInnerEnvelope().getContents());
		
		// encryption round 1: convergent encryption (using id)
		var encryptRound1 = SymmetricEncryption.encrypt(contents, container.getConvergentKey());
		
		// encryption round 2: secret key
		var encryptRound2 = SymmetricEncryption.encrypt(encryptRound1, secretKey);
		
		return new EncryptedContainer(
			new EncryptedEnvelope(
				container.getId(),
				encryptRound2,
				container.getInnerEnvelope().getLinkVerifier(),
				container.getInnerEnvelope().getSignature()
			),
			container.getLinks(),
			container.getSignature()
		);
	}
	
	public StoredContainer decrypt(EncryptedContainer container) throws EncryptionException {		
		// decryption round 1: secret key
		var decryptRound1 = SymmetricEncryption.decrypt(container.getInnerEnvelope().getContents(), secretKey);
		
		// decryption round 2: convergent encryption (using id)
		var decryptRound2 = SymmetricEncryption.decrypt(decryptRound1, container.getConvergentKey());
		
		return new StoredContainer(
			new Envelope(
				JSON.decode(decryptRound2, Contents.class),
				container.getInnerEnvelope().getLinkVerifier(),
				container.getInnerEnvelope().getSignature()
			),
			container.getLinks(),
			container.getSignature()
		);
	}
}
