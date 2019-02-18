package com.goodforgoodbusiness.engine.crypto.primitive;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.util.NodeFactoryExtra;

import com.goodforgoodbusiness.engine.ContainerBuilder;
import com.goodforgoodbusiness.engine.crypto.AsymmetricEncryption;
import com.goodforgoodbusiness.engine.crypto.Identity;
import com.goodforgoodbusiness.engine.crypto.SymmetricEncryption;
import com.goodforgoodbusiness.model.Link;
import com.goodforgoodbusiness.model.Link.RelType;
import com.goodforgoodbusiness.model.SubmittableContainer;
import com.goodforgoodbusiness.shared.encode.JSON;

public class ConvergentEncryptionTest {
	public static void main(String[] args) throws Exception {
		var kp = AsymmetricEncryption.createKeyPair();
		var id = new Identity("foo", kp.getPrivate().toEncodedString(), kp.getPublic().toEncodedString());
		
		var containerBuilder = new ContainerBuilder(id);
		
		var submittedContainer = new SubmittableContainer();
		
		submittedContainer.added(
			new Triple(
				NodeFactory.createURI("https://twitter.com/ijmad"),
				NodeFactory.createURI("http://xmlns.com/foaf/0.1/name"),
				NodeFactoryExtra.createLiteralNode("Ian Maddison", null, "http://www.w3.org/2001/XMLSchema#string")
			)
		);
		
		submittedContainer.linked(new Link(
			"b62accf26d5a1d8a7cb320e689ae2dd189a18cc3dca9457194e3d304e912c51d" +
			"adf746293e4707ec23a049e2cdb5684b2dcff91f5883e576d6a81100bafa56e4",
			RelType.CAUSED_BY
		));
		
		var storedContainer = containerBuilder.buildFrom(submittedContainer);
		
		// now try convergent encryption
		
		var json = JSON.encodeToString(storedContainer);
		
		var ciphertext = SymmetricEncryption.encrypt(json, storedContainer.getConvergentKey());
		var cleartext = SymmetricEncryption.decrypt(ciphertext, storedContainer.getConvergentKey());
		
		System.out.println(cleartext.equals(json));
	}
}
