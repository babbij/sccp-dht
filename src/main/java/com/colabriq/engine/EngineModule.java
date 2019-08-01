package com.goodforgoodbusiness.engine;

import static com.goodforgoodbusiness.shared.ConfigLoader.loadConfig;
import static com.goodforgoodbusiness.shared.GuiceUtil.o;
import static com.google.inject.Guice.createInjector;
import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static org.apache.commons.configuration2.ConfigurationConverter.getProperties;

import java.util.Properties;
import java.util.concurrent.ExecutorService;

import org.apache.commons.configuration2.Configuration;
import org.apache.log4j.Logger;
import org.rocksdb.RocksDBException;

import com.goodforgoodbusiness.engine.backend.Warp;
import com.goodforgoodbusiness.engine.backend.Weft;
import com.goodforgoodbusiness.engine.backend.impl.rocks.ContainerStore;
import com.goodforgoodbusiness.engine.backend.impl.rocks.PointerStore;
import com.goodforgoodbusiness.engine.webapp.rpc.DHTRPCHandler;
import com.goodforgoodbusiness.engine.webapp.rpc.call.ContainerFetch;
import com.goodforgoodbusiness.engine.webapp.rpc.call.ContainerPublish;
import com.goodforgoodbusiness.engine.webapp.rpc.call.ContainerSearch;
import com.goodforgoodbusiness.engine.webapp.rpc.call.PointerPublish;
import com.goodforgoodbusiness.engine.webapp.rpc.call.PointerSearch;
import com.goodforgoodbusiness.rocks.RocksManager;
import com.goodforgoodbusiness.rpclib.server.receiver.RPCReceiver;
import com.goodforgoodbusiness.shared.LogConfigurer;
import com.goodforgoodbusiness.shared.executor.ExecutorProvider;
import com.goodforgoodbusiness.shared.executor.PrioritizedExecutor;
import com.goodforgoodbusiness.webapp.BaseServer;
import com.goodforgoodbusiness.webapp.BaseVerticle;
import com.goodforgoodbusiness.webapp.BaseVerticle.HandlerProvider;
import com.goodforgoodbusiness.webapp.VertxProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

/**
 * Configure + start up a basic engine module
 */
public class EngineModule extends AbstractModule {
	private static final Logger log = Logger.getLogger(EngineModule.class);
	
	private final Configuration config;
	private final Injector injector;
	
	private BaseServer server = null;
	
	public EngineModule(Configuration config) {
		this.config = config;
		this.injector = createInjector(this);
	}
	
	@Override
	protected void configure() {
		binder().requireExplicitBindings();
			
		Properties props = getProperties(config);
		Names.bindProperties(binder(), props);
		
		try {
			// create and start the database 
			var rocksManager = new RocksManager(props.getProperty("storage.path"));
			rocksManager.start();
			
			bind(RocksManager.class).toInstance(rocksManager);
		}
		catch (RocksDBException e) {
			throw new RuntimeException("RocksDB failed to start", e);
		}
		
		bind(Warp.class).to(PointerStore.class);
		bind(Weft.class).to(ContainerStore.class);
		
		bind(ExecutorService.class).toProvider(ExecutorProvider.class);
		
		// bind Vert.x components
		bind(Vertx.class).toProvider(VertxProvider.class);
		bind(BaseServer.class);
		bind(BaseVerticle.class);
		bind(Verticle.class).to(BaseVerticle.class);
		
		// bind rpc handlers
		bind(DHTRPCHandler.class);
		
		// bind rpc calls
		var callBinder = newSetBinder(binder(), RPCReceiver.class);
		
		callBinder.addBinding().to(ContainerFetch.class);
		callBinder.addBinding().to(ContainerPublish.class);
		callBinder.addBinding().to(ContainerSearch.class);
		callBinder.addBinding().to(PointerPublish.class);
		callBinder.addBinding().to(PointerSearch.class);
		
		// configure route mappings
		// fine to use getProvider here because it won't be called until the injector is created
		bind(HandlerProvider.class).toInstance((router) -> {
			router.post("/rpc").handler(o(injector, DHTRPCHandler.class));
		});
	}
	
	public void start() {
		log.info("Starting services...");
		
		// start endpoints
		this.server = injector.getInstance(Key.get(BaseServer.class));
		this.server.start();
	}
	
	public void shutdown() {
		if (this.server != null) {
			// carefully close those pieces that need shutting down
			this.server.stop();
			this.server = null;
			
			var vx = injector.getInstance(Vertx.class);
			vx.close(voidResult -> {
				log.info("Vert.x closed");
				
				var es = injector.getInstance(PrioritizedExecutor.class);
				es.safeStop();
				
				var rm = injector.getInstance(RocksManager.class);
				rm.close();
			});
		}
	}
	
	public static void main(String[] args) throws Exception {
		var config = loadConfig(EngineModule.class, args.length > 0 ? args[0] : "env.properties");
		LogConfigurer.init(EngineModule.class, config.getString("log.properties", "log4j.properties"));
		new EngineModule(config).start();
	}
}
