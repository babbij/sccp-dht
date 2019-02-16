package com.goodforgoodbusiness.engine.crypto;

import com.goodforgoodbusiness.engine.crypto.primitive.EncryptionException;
import com.goodforgoodbusiness.engine.crypto.primitive.SymmetricEncryption;
import com.goodforgoodbusiness.engine.crypto.primitive.key.EncodeableSecretKey;
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
		var contents = JSON.encodeToString(claim.getInnerEnvelope().getContents());
		
		// encryption round 1: convergent encryption (using id)
		var encryptRound1 = SymmetricEncryption.encrypt(contents, claim.getConvergentKey());
		
		// encryption round 2: secret key
		var encryptRound2 = SymmetricEncryption.encrypt(encryptRound1, secretKey);
		
		return new EncryptedClaim(
			new EncryptedEnvelope(
				claim.getId(),
				encryptRound2,
				claim.getInnerEnvelope().getLinkVerifier(),
				claim.getInnerEnvelope().getSignature()
			),
			claim.getLinks(),
			claim.getSignature()
		);
	}
	
	public StoredClaim decrypt(EncryptedClaim claim) throws EncryptionException {		
		// decryption round 1: secret key
		var decryptRound1 = SymmetricEncryption.decrypt(claim.getInnerEnvelope().getContents(), secretKey);
		
		// decryption round 2: convergent encryption (using id)
		var decryptRound2 = SymmetricEncryption.decrypt(decryptRound1, claim.getConvergentKey());
		
		return new StoredClaim(
			new Envelope(
				JSON.decode(decryptRound2, Contents.class),
				claim.getInnerEnvelope().getLinkVerifier(),
				claim.getInnerEnvelope().getSignature()
			),
			claim.getLinks(),
			claim.getSignature()
		);
	}
}
