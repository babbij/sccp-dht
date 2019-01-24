package com.goodforgoodbusiness.dhtjava;

import static com.goodforgoodbusiness.shared.ConfigLoader.loadConfig;
import static com.google.inject.Guice.createInjector;
import static org.apache.commons.configuration2.ConfigurationConverter.getProperties;

import java.security.InvalidKeyException;
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
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class DHTModule extends AbstractModule {
	private final Configuration config;
	
	public DHTModule(Configuration config) {
		this.config = config;
	}
	
	@Override
	protected void configure() {
		binder().requireExplicitBindings();
		
		try {
			Properties props = getProperties(config);
			Names.bindProperties(binder(), props);
			
			bind(Identity.class);
			bind(ClaimBuilder.class);
			bind(PointerCrypter.class);
			bind(DHTStore.class).to(MongoDHTStore.class);
			bind(ShareKeyStore.class).to(MongoKeyStore.class);
			
			bind(DHTService.class);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Provides @Singleton
	protected KPABEInstance getKPABE(
		@Named("kpabe.publicKey") String publicKey,
		@Named("kpabe.secretKey") String privateKey) throws InvalidKeyException {
		return KPABEInstance.forKeys(publicKey, privateKey);
	}
	
	public static void main(String[] args) throws Exception {
		createInjector(new DHTModule(loadConfig(DHTModule.class, "dht.properties")))
			.getInstance(DHTService.class)
			.start()
		;
	}
}
