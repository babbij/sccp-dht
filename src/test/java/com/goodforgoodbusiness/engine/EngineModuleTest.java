package com.goodforgoodbusiness.engine;

import static com.goodforgoodbusiness.shared.ConfigLoader.loadConfig;
import static com.google.inject.Guice.createInjector;

import com.goodforgoodbusiness.webapp.Webapp;

public class EngineModuleTest {
	public static void main(String[] args) throws Exception {
		var configFile = args.length > 0 ? args[0] : "engine.properties";
		
		createInjector(new EngineModule(loadConfig(EngineModuleTest.class, configFile)))
			.getInstance(Webapp.class)
			.start()
		;
	}
}
