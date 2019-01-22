package com.goodforgoodbusiness.dhtjava.crypto;

import static java.util.stream.Collectors.joining;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.util.stream.Stream;

import javax.crypto.SecretKey;

import com.goodforgoodbusiness.kpabe.KPABEException;
import com.goodforgoodbusiness.kpabe.KPABEInstance;
import com.goodforgoodbusiness.kpabe.key.KPABEPublicKey;
import com.goodforgoodbusiness.kpabe.key.KPABESecretKey;
import com.goodforgoodbusiness.shared.JSON;
import com.goodforgoodbusiness.shared.model.Contents;
import com.goodforgoodbusiness.shared.model.EncryptedClaim;
import com.goodforgoodbusiness.shared.model.EncryptedEnvelope;
import com.goodforgoodbusiness.shared.model.Envelope;
import com.goodforgoodbusiness.shared.model.Pointer;
import com.goodforgoodbusiness.shared.model.StoredClaim;

public class Crypto {
	private final KPABEInstance kpabe;
	
	public Crypto(String publicKey, String secretKey) throws InvalidKeyException {
		this(KPABEInstance.forKeys(
			new KPABEPublicKey(publicKey),
			new KPABESecretKey(secretKey)
		));
	}
	
	public Crypto(KPABEInstance kpabe) {
		this.kpabe = kpabe;
	}
	
	public String encryptPointer(Pointer pointer, Stream<String> attributes) throws KPABEException {
		return kpabe.encrypt(
			JSON.encodeToString(pointer),
			attributes.collect( joining( "|" ) )
		);
	}
	
	public Pointer decryptPointer(String data, KeyPair shareKey) throws KPABEException, InvalidKeyException {
		var decrypted = KPABEInstance.decrypt(data, shareKey);
		
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
