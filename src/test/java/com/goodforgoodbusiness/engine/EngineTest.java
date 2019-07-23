package com.goodforgoodbusiness.engine;

import static com.goodforgoodbusiness.shared.ConfigLoader.loadConfig;

import com.goodforgoodbusiness.engine.EngineModule;
import com.goodforgoodbusiness.shared.LogConfigurer;

public class EngineTest {
	public static void main(String[] args) throws Exception {
		var config = loadConfig(EngineTest.class, "test.properties");
		LogConfigurer.init(EngineModule.class, "log4j.properties");
		new EngineModule(config).start();
	}
}
