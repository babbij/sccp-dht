package com.goodforgoodbusiness.engine;

import static com.goodforgoodbusiness.engine.crypto.AsymmetricEncryption.sign;
import static java.util.stream.Collectors.toList;

import java.security.PrivateKey;
import java.util.stream.Collectors;

import com.goodforgoodbusiness.engine.crypto.AsymmetricEncryption;
import com.goodforgoodbusiness.engine.crypto.EncryptionException;
import com.goodforgoodbusiness.engine.crypto.Identity;
import com.goodforgoodbusiness.model.Contents;
import com.goodforgoodbusiness.model.Contents.ContentsType;
import com.goodforgoodbusiness.model.Envelope;
import com.goodforgoodbusiness.model.Link;
import com.goodforgoodbusiness.model.LinkSecret;
import com.goodforgoodbusiness.model.LinkVerifier;
import com.goodforgoodbusiness.model.ProvenLink;
import com.goodforgoodbusiness.model.Signature;
import com.goodforgoodbusiness.model.StorableContainer;
import com.goodforgoodbusiness.model.SubmittableContainer;
import com.goodforgoodbusiness.shared.encode.CBOR;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Builds a storable/encryptable container out of what's submitted.
 */
@Singleton
public class ContainerBuilder {
	private final Identity identity;
	
	@Inject
	public ContainerBuilder(Identity identity) {
		this.identity = identity;
	}
	
	public StorableContainer buildFrom(SubmittableContainer container) throws EncryptionException {
		try {
			final var linkSigningPair = AsymmetricEncryption.createKeyPair();
			
			final var contents = new Contents(
				ContentsType.CLAIM,
				container.getLinks()
					.stream()
					.map(link -> antecedent(link, linkSigningPair.getPrivate()))
					.collect(toList()),
				container.getAdded(), 
				container.getRemoved(), 
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
			
			final var provedLinks = container.getLinks()
				.stream()
				.map(link -> new ProvenLink(link, linkProof(innerEnvelope.getHashKey(), link, linkSigningPair.getPrivate())))
				.collect(Collectors.toSet());
			
			return new StorableContainer(
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
