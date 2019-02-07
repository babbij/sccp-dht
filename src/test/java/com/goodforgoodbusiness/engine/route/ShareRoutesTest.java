package com.goodforgoodbusiness.engine.route;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.goodforgoodbusiness.engine.crypto.KeyManager;
import com.goodforgoodbusiness.engine.crypto.ShareKeyCreator;
import com.goodforgoodbusiness.engine.dht.DHTAccessGovernor;
import com.goodforgoodbusiness.engine.store.keys.impl.MemKeyStore;
import com.goodforgoodbusiness.kpabe.local.KPABELocalInstance;
import com.goodforgoodbusiness.model.TriTuple;

import spark.Request;
import spark.Response;

public class ShareRoutesTest {
	public static void main(String[] args) throws Exception {
		var kpabe = KPABELocalInstance.newKeys();
		var keyManager = new KeyManager(kpabe.getPublicKey(), kpabe.getSecretKey());
		
		var scc = new ShareKeyCreator(keyManager);
		var keyStore = new MemKeyStore();
		
		var requestRoute = new ShareRequestRoute(scc);
		var acceptRoute = new ShareAcceptRoute(keyStore, new DHTAccessGovernor(false, 0));
		
		var req1 = mock(Request.class);
		when(req1.queryParams("sub")).thenReturn("s");
		when(req1.queryParams("pre")).thenReturn("p");
		when(req1.queryParams("obj")).thenReturn("o");
		
		var res1 = mock(Response.class);
		
		var output1 = requestRoute.handle(req1, res1);
		System.out.println(output1);
		
		var req2 = mock(Request.class);
		when(req2.body()).thenReturn(output1.toString());
		
		var res2 = mock(Response.class);
		
		acceptRoute.handle(req2, res2);
		var output2 = acceptRoute.handle(req2, res2);
		System.out.println(output2);
		
		// verify in the store
		var searchKeys = keyStore.knownSharers(new TriTuple(Optional.of("s"), Optional.of("p"), Optional.of("o")));
		searchKeys.forEach(searchKey -> {
			System.out.println(searchKey);
		});
		
		var decryptKeys = keyStore.keysForDecrypt(kpabe.getPublicKey());
		decryptKeys.forEach(decryptKey -> {
			System.out.println(decryptKey);
		});
	}
}
