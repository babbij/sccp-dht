package com.goodforgoodbusiness.dhtjava.service.route;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.dhtjava.crypto.PointerCrypter;
import com.goodforgoodbusiness.dhtjava.crypto.primitive.key.EncodeableShareKey;
import com.goodforgoodbusiness.dhtjava.crypto.store.spec.ShareKeySpec;
import com.goodforgoodbusiness.dhtjava.crypto.store.spec.ShareRangeSpec;
import com.goodforgoodbusiness.shared.encode.JSON;
import com.goodforgoodbusiness.shared.web.ContentType;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.inject.Inject;

import spark.Request;
import spark.Response;
import spark.Route;

public class ShareRequestRoute implements Route {
	private static final Logger log = Logger.getLogger(ShareRequestRoute.class);
	
	public static class ShareResponse {
		@Expose
		@SerializedName("pattern")
		private ShareKeySpec spec;

		@Expose
		@SerializedName("range")
		private ShareRangeSpec range;
		
		@Expose
		@SerializedName("key")
		private EncodeableShareKey key;
		
		public ShareResponse(ShareKeySpec spec, ShareRangeSpec range, EncodeableShareKey key) {
			this.spec = spec;
			this.range = range;
			this.key = key;
		}
	}
	
	private final PointerCrypter pointerCrypter;
	
	@Inject
	public ShareRequestRoute(PointerCrypter pointerCrypter) {
		this.pointerCrypter = pointerCrypter;
	}
	
	@Override
	public Object handle(Request req, Response res) throws Exception {
		res.type(ContentType.json.getContentTypeString());
		
		log.info("Processing share request");
		
		var keySpec = new ShareKeySpec(
			req.queryParams("sub"),
			req.queryParams("pre"),
			req.queryParams("obj")
		);
		
		var rangeSpec = new ShareRangeSpec(
			req.queryParams("start"), req.queryParams("end")
		);
		
		var shareKey = pointerCrypter.makeShareKey(keySpec, rangeSpec);
		
		return JSON.encode(
			new ShareResponse(keySpec, rangeSpec, shareKey)
		);
	}
}
