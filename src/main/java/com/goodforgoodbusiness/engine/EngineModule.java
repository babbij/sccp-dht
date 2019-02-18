package com.goodforgoodbusiness.engine;

import static com.goodforgoodbusiness.shared.ConfigLoader.loadConfig;
import static com.goodforgoodbusiness.webapp.Resource.get;
import static com.goodforgoodbusiness.webapp.Resource.post;
import static com.google.inject.Guice.createInjector;
import static com.google.inject.multibindings.MapBinder.newMapBinder;
import static org.apache.commons.configuration2.ConfigurationConverter.getProperties;

import java.util.Properties;

import org.apache.commons.configuration2.Configuration;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.engine.backend.DHTBackend;
import com.goodforgoodbusiness.engine.backend.impl.remote.RemoteDHTBackend;
import com.goodforgoodbusiness.engine.backend.impl.remote.RemoteDHTSupport;
import com.goodforgoodbusiness.engine.crypto.Identity;
import com.goodforgoodbusiness.engine.route.ContainerSubmitRoute;
import com.goodforgoodbusiness.engine.route.MatchSearchRoute;
import com.goodforgoodbusiness.engine.route.PingRoute;
import com.goodforgoodbusiness.engine.route.ShareAcceptRoute;
import com.goodforgoodbusiness.engine.route.ShareRequestRoute;
import com.goodforgoodbusiness.engine.store.container.ContainerStore;
import com.goodforgoodbusiness.engine.store.container.impl.CachingContainerStore;
import com.goodforgoodbusiness.engine.store.container.impl.CachingContainerStore.Underlying;
import com.goodforgoodbusiness.engine.store.container.impl.MongoContainerStore;
import com.goodforgoodbusiness.engine.store.keys.ShareKeyStore;
import com.goodforgoodbusiness.engine.store.keys.impl.MongoKeyStore;
import com.goodforgoodbusiness.engine.warp.Warp;
import com.goodforgoodbusiness.engine.weft.Weft;
import com.goodforgoodbusiness.shared.LogConfigurer;
import com.goodforgoodbusiness.webapp.Resource;
import com.goodforgoodbusiness.webapp.Webapp;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import spark.Route;

public class EngineModule extends AbstractModule {
	private static final Logger log = Logger.getLogger(EngineModule.class);
	
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
			
			bind(Governer.class);
			bind(Publisher.class);
			bind(Searcher.class);
			bind(ContainerBuilder.class);
			
			bind(Warp.class);
			bind(Weft.class);
			
			bind(ShareManager.class);
			
			bind(DHTBackend.class).to(RemoteDHTBackend.class);
			bind(RemoteDHTSupport.class);
			
			bind(ShareKeyStore.class).to(MongoKeyStore.class);
			
			if (config.getBoolean("claimstore.cache.enabled", false)) {
				bind(ContainerStore.class).to(CachingContainerStore.class);
				bind(ContainerStore.class).annotatedWith(Underlying.class).to(MongoContainerStore.class);
			}
			else {
				log.warn("Container cache is DISABLED");
				bind(ContainerStore.class).to(MongoContainerStore.class);
			}
			
			bind(Webapp.class);
			
			var routes = newMapBinder(binder(), Resource.class, Route.class);
			
			routes.addBinding(get("/ping")).to(PingRoute.class);
			
			routes.addBinding(get("/matches")).to(MatchSearchRoute.class);
			routes.addBinding(post("/containers")).to(ContainerSubmitRoute.class);
			routes.addBinding(get("/share")).to(ShareRequestRoute.class);
			routes.addBinding(post("/share")).to(ShareAcceptRoute.class);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void main(String[] args) throws Exception {
		var config = loadConfig(EngineModule.class, args.length > 0 ? args[0] : "env.properties");
		LogConfigurer.init(EngineModule.class, config.getString("log.properties", "log4j.properties"));
		
		createInjector(new EngineModule(config))
			.getInstance(Webapp.class)
			.start()
		;
	}
}
