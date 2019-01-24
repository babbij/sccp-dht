package com.goodforgoodbusiness.dhtjava;

import static com.goodforgoodbusiness.shared.ConfigLoader.loadConfig;
import static com.google.inject.Guice.createInjector;
import static org.apache.commons.configuration2.ConfigurationConverter.getProperties;

import java.util.Properties;

import org.apache.commons.configuration2.Configuration;

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
	private final Configuration config;
	
	protected DHTModule(Configuration config) {
		this.config = config;
	}
	
	@Override
	protected void configure() {
		try {
			Properties props = getProperties(config);
			Names.bindProperties(binder(), props);
			
			bind(KPABEInstance.class).toInstance(
				KPABEInstance.forKeys(
					props.getProperty("kpabe.publicKey"),
					props.getProperty("kpabe.secretKey")
				)
			);
			
			bind(Identity.class);
			bind(PointerCrypter.class);
			bind(DHTStore.class).to(MongoDHTStore.class);
			bind(ShareKeyStore.class).to(MongoKeyStore.class);
			
			bind(DHTService.class);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void main(String[] args) throws Exception {
		createInjector(new DHTModule(loadConfig(DHTModule.class, "config.properties")))
			.getInstance(DHTService.class)
			.start()
		;
	}
}
