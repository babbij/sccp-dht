package com.goodforgoodbusiness.dhtjava;

import static com.goodforgoodbusiness.dhtjava.crypto.primitive.AsymmetricEncryption.sign;
import static java.util.stream.Collectors.toList;

import java.security.PrivateKey;
import java.util.stream.Collectors;

import com.goodforgoodbusiness.dhtjava.crypto.Identity;
import com.goodforgoodbusiness.dhtjava.crypto.primitive.AsymmetricEncryption;
import com.goodforgoodbusiness.dhtjava.crypto.primitive.EncryptionException;
import com.goodforgoodbusiness.model.Contents;
import com.goodforgoodbusiness.model.Contents.ThreadType;
import com.goodforgoodbusiness.model.Envelope;
import com.goodforgoodbusiness.model.Link;
import com.goodforgoodbusiness.model.LinkSecret;
import com.goodforgoodbusiness.model.LinkVerifier;
import com.goodforgoodbusiness.model.ProvenLink;
import com.goodforgoodbusiness.model.Signature;
import com.goodforgoodbusiness.model.StoredClaim;
import com.goodforgoodbusiness.model.SubmittableClaim;
import com.goodforgoodbusiness.shared.encode.CBOR;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Builds a storable/encryptable claim out of what's submitted.
 */
@Singleton
public class ClaimBuilder {
	private final Identity identity;
	
	@Inject
	public ClaimBuilder(Identity identity) {
		this.identity = identity;
	}
	
	public StoredClaim buildFrom(SubmittableClaim claim) throws EncryptionException {
		try {
			final var linkSigningPair = AsymmetricEncryption.createKeyPair();
			
			final var contents = new Contents(
				ThreadType.CLAIM,
				claim.getLinks()
					.stream()
					.map(link -> antecedent(link, linkSigningPair.getPrivate()))
					.collect(toList()),
				claim.getAdded(), 
				claim.getRemoved(), 
				new LinkSecret(
					linkSigningPair.getPrivate().getAlgorithm(), 
					linkSigningPair.getPrivate().toEncodedString()
				)
			);
			
			final var linkVerifier = new LinkVerifier(
				linkSigningPair.getPublic().getAlgorithm(),
				linkSigningPair.getPublic().toEncodedString()
			);
			
			final var innerEnvelope = new Envelope(
				contents,
				linkVerifier,
				new Signature(
					identity.getDID(),
					identity.getPrivate().getAlgorithm(),
					signature(identity, contents, linkVerifier)
				)
			);
			
			final var provedLinks = claim.getLinks()
				.stream()
				.map(link -> new ProvenLink(link, linkProof(innerEnvelope.getHashKey(), link, linkSigningPair.getPrivate())))
				.collect(Collectors.toSet());
			
			return new StoredClaim(
				innerEnvelope,
				provedLinks,
				new Signature(
					identity.getDID(),
					identity.getPrivate().getAlgorithm(),
					signature(identity, innerEnvelope, provedLinks)
				)
			);
		}
		catch (RuntimeException e) {
			if (e.getCause() instanceof EncryptionException) {
				throw (EncryptionException)e.getCause();
			}
			
			throw e;
		}
	}
	
	private static String signature(Identity identity, Object... objects) throws EncryptionException {
		return identity.sign(CBOR.forObject(objects));
	}
	
	private static String antecedent(Link link, PrivateKey privateKey) {
		try {
			return sign(CBOR.forObject(link), privateKey);
		}
		catch (EncryptionException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static String linkProof(String hashkey, Link link, PrivateKey privateKey) {
		try {
			return sign(CBOR.forObject(new Object [] { hashkey, link }), privateKey);
		}
		catch (EncryptionException e) {
			throw new RuntimeException(e);
		}
	}
}
