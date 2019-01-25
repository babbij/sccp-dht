package com.goodforgoodbusiness.engine;

import static com.goodforgoodbusiness.shared.ConfigLoader.loadConfig;
import static com.goodforgoodbusiness.webapp.Resource.get;
import static com.goodforgoodbusiness.webapp.Resource.post;
import static com.google.inject.Guice.createInjector;
import static com.google.inject.multibindings.MapBinder.newMapBinder;
import static org.apache.commons.configuration2.ConfigurationConverter.getProperties;

import java.security.InvalidKeyException;
import java.util.Properties;

import org.apache.commons.configuration2.Configuration;

import com.goodforgoodbusiness.engine.crypto.Identity;
import com.goodforgoodbusiness.engine.crypto.PointerCrypter;
import com.goodforgoodbusiness.engine.dht.DHT;
import com.goodforgoodbusiness.engine.dht.DHTPublisher;
import com.goodforgoodbusiness.engine.dht.DHTSearcher;
import com.goodforgoodbusiness.engine.dht.impl.MongoDHT;
import com.goodforgoodbusiness.engine.route.ClaimSubmitRoute;
import com.goodforgoodbusiness.engine.route.MatchSearchRoute;
import com.goodforgoodbusiness.engine.route.ShareAcceptRoute;
import com.goodforgoodbusiness.engine.route.ShareRequestRoute;
import com.goodforgoodbusiness.engine.store.claim.ClaimStore;
import com.goodforgoodbusiness.engine.store.claim.impl.MemClaimStore;
import com.goodforgoodbusiness.engine.store.keys.ShareKeyStore;
import com.goodforgoodbusiness.engine.store.keys.impl.MongoKeyStore;
import com.goodforgoodbusiness.kpabe.KPABEInstance;
import com.goodforgoodbusiness.webapp.Resource;
import com.goodforgoodbusiness.webapp.Webapp;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import spark.Route;

public class EngineModule extends AbstractModule {
	private final Configuration config;
	
	public EngineModule(Configuration config) {
		this.config = config;
	}
	
	@Override
	protected void configure() {
		binder().requireExplicitBindings();
		
		try {
			Properties props = getProperties(config);
			Names.bindProperties(binder(), props);
			
			bind(Identity.class);
			bind(DHTPublisher.class);
			bind(DHTSearcher.class);
			bind(ClaimBuilder.class);
			bind(ClaimStore.class).to(MemClaimStore.class);
			bind(PointerCrypter.class);
			bind(DHT.class).to(MongoDHT.class);
			bind(ShareKeyStore.class).to(MongoKeyStore.class);
			bind(Webapp.class);
			
			var routes = newMapBinder(binder(), Resource.class, Route.class);
			
			routes.addBinding(get("/matches")).to(MatchSearchRoute.class);
			routes.addBinding(post("/claims")).to(ClaimSubmitRoute.class);
			routes.addBinding(get("/share")).to(ShareRequestRoute.class);
			routes.addBinding(post("/share")).to(ShareAcceptRoute.class);
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
		createInjector(new EngineModule(loadConfig(EngineModule.class, "engine.properties")))
			.getInstance(Webapp.class)
			.start()
		;
	}
}
