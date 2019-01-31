package com.goodforgoodbusiness.engine.dht.impl;

import static com.goodforgoodbusiness.shared.ConfigLoader.loadConfig;
import static com.google.inject.Guice.createInjector;

import com.goodforgoodbusiness.dht.RosterController;
import com.goodforgoodbusiness.engine.EngineModule;
import com.goodforgoodbusiness.webapp.Webapp;

public class RemoteDHTTest {
	public static void main(String[] args) throws Exception {
		RosterController.start(8050);
		
		createInjector(new EngineModule(loadConfig(EngineModule.class, "engine.properties")))
			.getInstance(Webapp.class)
			.start()
		;
	}
}
