package com.goodforgoodbusiness.engine;

import java.util.Random;
import java.util.stream.Stream;

import com.goodforgoodbusiness.rpclib.client.RPCWebClientCreator;
import com.goodforgoodbusiness.shared.LogConfigurer;
import com.goodforgoodbusiness.webapp.VertxProvider;

import io.vertx.core.Future;

public class DHTTestClient {
	private static int POINTER_PATTERN_SZ = 128;
	private static int POINTER_DATA_SZ = 400;
	
	private static final Random random = new Random();
	
	public static void main(String[] args) throws Exception {
		LogConfigurer.init(EngineModule.class, "log4j.properties");
		
		var vertx = new VertxProvider().get();
		var client = RPCWebClientCreator.create(vertx);
		var backend = new DHTRPCBackend(vertx, client, "http://localhost:8091");
				
		// this is a representative pointer size and a real pattern size
		var pattern1 = createPattern(POINTER_PATTERN_SZ);
		var pointer1 = new byte[POINTER_DATA_SZ];
		
		random.nextBytes(pointer1);
		backend.publishPointer(pattern1, pointer1, Future.<Void>future().setHandler(result -> {
			System.out.println("PUBLISHED!");
		}));
		
		var pointer2 = new byte[POINTER_DATA_SZ];
		random.nextBytes(pointer2);
		backend.publishPointer(pattern1, pointer2, Future.<Void>future().setHandler(result -> {
			System.out.println("PUBLISHED!");
		}));
		
		for (int i = 0; i < 10; i++) {
			final int ii = i;
			
			backend.searchForPointers(pattern1, Future.<Stream<byte[]>>future().setHandler(result -> {
				System.out.println("returned " + ii);
				
				if (result.succeeded()) {
					result.result().forEach(arr -> {
						System.out.println("result");
					});
				}
				else {
					result.cause().printStackTrace();
				}
			}));
		}
	}
	

	public static String createPattern(int length) {
		StringBuilder sb = new StringBuilder();
		while (sb.length() < length) {
			sb.append(Integer.toHexString(random.nextInt()));
		}
		
		return sb.toString();
	}
}


