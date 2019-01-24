package com.goodforgoodbusiness.dhtjava.crypto;

import com.goodforgoodbusiness.dhtjava.crypto.primitive.EncryptionException;
import com.goodforgoodbusiness.dhtjava.crypto.primitive.SymmetricEncryption;
import com.goodforgoodbusiness.dhtjava.crypto.primitive.key.EncodeableSecretKey;
import com.goodforgoodbusiness.model.Contents;
import com.goodforgoodbusiness.model.EncryptedClaim;
import com.goodforgoodbusiness.model.EncryptedEnvelope;
import com.goodforgoodbusiness.model.Envelope;
import com.goodforgoodbusiness.model.StoredClaim;
import com.goodforgoodbusiness.shared.encode.JSON;

public class ClaimCrypter {
	private final EncodeableSecretKey secretKey;
	
	/**
	 * Create brand new key
	 */
	public ClaimCrypter() {
		this.secretKey = SymmetricEncryption.createKey();
	}
	
	/**
	 * Create using existing key
	 */
	public ClaimCrypter(String encodedForm) {
		this.secretKey = new EncodeableSecretKey(encodedForm);
	}
	
	public EncodeableSecretKey getSecretKey() {
		return secretKey;
	}

	public EncryptedClaim encrypt(StoredClaim claim) throws EncryptionException {
		var encryptedContents = SymmetricEncryption.encrypt(
			JSON.encodeToString(claim.getInnerEnvelope().getContents()),
			secretKey
		);
		
		return new EncryptedClaim(
			new EncryptedEnvelope(
				claim.getId(),
				encryptedContents,
				claim.getInnerEnvelope().getLinkVerifier(),
				claim.getInnerEnvelope().getSignature()
			),
			claim.getLinks(),
			claim.getSignature()
		);
	}
	
	public StoredClaim decrypt(EncryptedClaim claim) throws EncryptionException {
		var contents = JSON.decode(
			SymmetricEncryption.decrypt(
				claim.getInnerEnvelope().getContents(),
				secretKey
			),
			Contents.class
		);
		
		return new StoredClaim(
			new Envelope(
				contents,
				claim.getInnerEnvelope().getLinkVerifier(),
				claim.getInnerEnvelope().getSignature()
			),
			claim.getLinks(),
			claim.getSignature()
		);
	}
}
