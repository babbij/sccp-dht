package com.colabriq.engine;

import static com.colabriq.shared.ConfigLoader.loadConfig;

import com.colabriq.engine.EngineModule;
import com.colabriq.shared.LogConfigurer;

public class EngineTest {
	public static void main(String[] args) throws Exception {
		var config = loadConfig(EngineTest.class, "test.properties");
		LogConfigurer.init(EngineModule.class, "log4j.properties");
		new EngineModule(config).start();
	}
}
