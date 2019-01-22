package com.goodforgoodbusiness.dhtjava;

import java.security.KeyPair;
import java.util.stream.Collectors;

import com.goodforgoodbusiness.dhtjava.crypto.CryptoException;
import com.goodforgoodbusiness.dhtjava.crypto.KeyEncoder;
import com.goodforgoodbusiness.shared.model.Contents;
import com.goodforgoodbusiness.shared.model.Envelope;
import com.goodforgoodbusiness.shared.model.Link;
import com.goodforgoodbusiness.shared.model.LinkSecret;
import com.goodforgoodbusiness.shared.model.LinkVerifier;
import com.goodforgoodbusiness.shared.model.ProvenLink;
import com.goodforgoodbusiness.shared.model.Signature;
import com.goodforgoodbusiness.shared.model.StoredClaim;
import com.goodforgoodbusiness.shared.model.SubmittableClaim;
import com.goodforgoodbusiness.shared.model.Contents.ContentType;
import com.google.inject.Singleton;

import static com.goodforgoodbusiness.dhtjava.crypto.Signing.sign;
import static com.goodforgoodbusiness.shared.EncodeUtil.*;
import static com.goodforgoodbusiness.dhtjava.crypto.Signing.generateKeyPair;

/**
 * Builds a storable/encryptable claim out of what's submitted.
 */
@Singleton
public class ClaimBuilder {
	private static final String DID = "did:abcd1";
	private static final KeyPair IDENTITY = generateKeyPair();
	
	public static StoredClaim buildFrom(SubmittableClaim claim) throws CryptoException {
		try {
			final var linkVerifierKeys = generateKeyPair();
			
			final var contents = new Contents(
				ContentType.CLAIM,
				claim.getLinks()
					.stream()
					.map(link -> antecedent(link, linkVerifierKeys))
					.collect(Collectors.toList()),
				claim.getAdded(), 
				claim.getRemoved(), 
				new LinkSecret(
					linkVerifierKeys.getPrivate().getAlgorithm(), 
					KeyEncoder.encodeKey(linkVerifierKeys.getPrivate())
				)
			);
			
			final var linkVerifier = new LinkVerifier(
				linkVerifierKeys.getPublic().getAlgorithm(),
				KeyEncoder.encodeKey(linkVerifierKeys.getPublic())
			);
			
			final var innerEnvelope = new Envelope(
				contents,
				linkVerifier,
				new Signature(DID, IDENTITY.getPrivate(), contents, linkVerifier)
			);
			
			final var provedLinks = claim.getLinks()
				.stream()
				.map(link -> new ProvenLink(link, linkProof(innerEnvelope.getHashKey(), link, linkVerifierKeys)))
				.collect(Collectors.toSet());
			
			return new StoredClaim(
				innerEnvelope,
				provedLinks,
				new Signature(DID, IDENTITY.getPrivate(), innerEnvelope, provedLinks)
			);
		}
		catch (RuntimeException e) {
			if (e.getCause() instanceof CryptoException) {
				throw (CryptoException)e.getCause();
			}
			
			throw e;
		}
	}
	
	private static String antecedent(Link link, KeyPair linkVerifier) {
		try {
			return sign(cborDigest(link), linkVerifier.getPrivate());
		}
		catch (CryptoException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static String linkProof(String hashkey, Link link, KeyPair linkVerifier) {
		try {
			return sign(cborDigest(new Object [] { hashkey, link }), linkVerifier.getPrivate());
		}
		catch (CryptoException e) {
			throw new RuntimeException(e);
		}
	}
}
