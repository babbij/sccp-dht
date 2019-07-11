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
import com.goodforgoodbusiness.engine.backend.DHTBackendOption;
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
			
			bind(Governer.class);
			bind(Searcher.class);
			
			if (config.containsKey("backend.impl")) {
				var backendClazz = DHTBackendOption.valueOf(config.getString("backend.impl"));
				bind(DHTBackend.class).to(backendClazz.getImplClass());
				if (backendClazz == DHTBackendOption.REMOTE) {
					bind(RemoteDHTSupport.class);
				}
			}
			else {
				throw new IllegalArgumentException("backend.impl was null");
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void start() {
		var injector = createInjector(this);
		
//		this.webapp = injector.getInstance(Webapp.class);
//		this.webapp.start();
	}
	
	public static void main(String[] args) throws Exception {
		var config = loadConfig(EngineModule.class, args.length > 0 ? args[0] : "env.properties");
		LogConfigurer.init(EngineModule.class, config.getString("log.properties", "log4j.properties"));
		new EngineModule(config).start();
	}
}
