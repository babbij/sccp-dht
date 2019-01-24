package com.goodforgoodbusiness.dhtjava;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.util.NodeFactoryExtra;

import com.goodforgoodbusiness.dhtjava.crypto.ClaimCrypter;
import com.goodforgoodbusiness.dhtjava.crypto.Identity;
import com.goodforgoodbusiness.dhtjava.crypto.primitive.AsymmetricEncryption;
import com.goodforgoodbusiness.model.Link;
import com.goodforgoodbusiness.model.Link.RelType;
import com.goodforgoodbusiness.model.SubmittableClaim;
import com.goodforgoodbusiness.shared.encode.JSON;

public class ClaimBuilderTest {
	public static void main(String[] args) throws Exception {
		var kp = AsymmetricEncryption.createKeyPair();
		var id = new Identity("foo", kp.getPrivate().toEncodedString(), kp.getPublic().toEncodedString());
		
		var crypter = new ClaimCrypter();
		var claimBuilder = new ClaimBuilder(id);
		
		var submittedClaim = new SubmittableClaim();
		
		submittedClaim.added(
			new Triple(
				NodeFactory.createURI("https://twitter.com/ijmad"),
				NodeFactory.createURI("http://xmlns.com/foaf/0.1/name"),
				NodeFactoryExtra.createLiteralNode("Ian Maddison", null, "http://www.w3.org/2001/XMLSchema#string")
			)
		);
		
		submittedClaim.linked(new Link(
			"b62accf26d5a1d8a7cb320e689ae2dd189a18cc3dca9457194e3d304e912c51d" +
			"adf746293e4707ec23a049e2cdb5684b2dcff91f5883e576d6a81100bafa56e4",
			RelType.CAUSED_BY
		));
		
		var storedClaim = claimBuilder.buildFrom(submittedClaim);
		
		var storedJson = JSON.encodeToString(storedClaim);
		System.out.println(storedJson);
		
		var encryptedClaim = crypter.encrypt(storedClaim);
		String encryptedJson = JSON.encodeToString(encryptedClaim);
		System.out.println(encryptedJson);
		
		var storedClaim2 = crypter.decrypt(encryptedClaim);
		System.out.println(storedClaim.getId());
		System.out.println(storedClaim2.getId());
	}
}
