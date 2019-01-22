package com.goodforgoodbusiness.dhtjava.crypto;

import static java.util.stream.Collectors.joining;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.util.stream.Stream;

import javax.crypto.SecretKey;

import com.goodforgoodbusiness.dhtjava.crypto.abe.ABE;
import com.goodforgoodbusiness.dhtjava.crypto.abe.ABEException;
import com.goodforgoodbusiness.dhtjava.crypto.abe.key.ABEPublicKey;
import com.goodforgoodbusiness.dhtjava.crypto.abe.key.ABESecretKey;
import com.goodforgoodbusiness.shared.JSON;
import com.goodforgoodbusiness.shared.model.Contents;
import com.goodforgoodbusiness.shared.model.EncryptedClaim;
import com.goodforgoodbusiness.shared.model.EncryptedEnvelope;
import com.goodforgoodbusiness.shared.model.Envelope;
import com.goodforgoodbusiness.shared.model.Pointer;
import com.goodforgoodbusiness.shared.model.StoredClaim;

public class Crypto {
	private final ABE abe;
	
	public Crypto(String publicKey, String secretKey) throws InvalidKeyException {
		this(ABE.forKeys(
			new ABEPublicKey(publicKey),
			new ABESecretKey(secretKey)
		));
	}
	
	public Crypto(ABE abe) {
		this.abe = abe;
	}
	
	public String encryptPointer(Pointer pointer, Stream<String> attributes) throws ABEException {
		return abe.encrypt(
			JSON.encodeToString(pointer),
			attributes.collect( joining( "|" ) )
		);
	}
	
	public Pointer decryptPointer(String data, KeyPair shareKey) throws ABEException, InvalidKeyException {
		var decrypted = ABE.decrypt(data, shareKey);
		
		if (decrypted != null) {
			// successful decryption
			return JSON.decode(decrypted, Pointer.class);
		}
		else {
			return null;
		}
	}

	public EncryptedClaim encryptClaim(StoredClaim claim, SecretKey key) throws CryptoException {
		var encryptedContents = Symmetric.encrypt(
			key,
			JSON.encodeToString(claim.getInnerEnvelope().getContents())
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
	
	public StoredClaim decryptClaim(EncryptedClaim claim, SecretKey key) throws CryptoException {
		var contents = JSON.decode(
			Symmetric.decrypt(
				key,
				claim.getInnerEnvelope().getContents()
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
