package com.goodforgoodbusiness.dhtjava;

import static com.google.inject.Guice.createInjector;

import java.util.Properties;

import com.goodforgoodbusiness.dhtjava.crypto.Identity;
import com.goodforgoodbusiness.dhtjava.crypto.PointerCrypter;
import com.goodforgoodbusiness.dhtjava.crypto.store.ShareKeyStore;
import com.goodforgoodbusiness.dhtjava.crypto.store.impl.MongoKeyStore;
import com.goodforgoodbusiness.dhtjava.dht.DHTStore;
import com.goodforgoodbusiness.dhtjava.dht.impl.MongoDHTStore;
import com.goodforgoodbusiness.dhtjava.service.DHTService;
import com.goodforgoodbusiness.kpabe.KPABEInstance;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class DHTModule extends AbstractModule {
	@Override
	protected void configure() {
		var props = new Properties();
		try (var is = getClass().getClassLoader().getResourceAsStream("main.properties")) {
			props.load(is);
			Names.bindProperties(binder(), props);
			
			bind(KPABEInstance.class).toInstance(
				KPABEInstance.forKeys(
					props.getProperty("kpabe.publicKey"),
					props.getProperty("kpabe.secretKey")
				)
			);
			
			bind(PointerCrypter.class);
			bind(Identity.class);
			bind(DHTStore.class).to(MongoDHTStore.class);
			bind(ShareKeyStore.class).to(MongoKeyStore.class);
			
			bind(DHTService.class);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void main(String[] args) throws Exception {
		createInjector(new DHTModule())
			.getInstance(DHTService.class)
			.start()
		;
	}
}
