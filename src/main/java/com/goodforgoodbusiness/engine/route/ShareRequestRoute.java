package com.goodforgoodbusiness.engine.route;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.engine.crypto.PointerCrypter;
import com.goodforgoodbusiness.engine.crypto.primitive.key.EncodeableShareKey;
import com.goodforgoodbusiness.engine.store.keys.spec.ShareKeySpec;
import com.goodforgoodbusiness.engine.store.keys.spec.ShareRangeSpec;
import com.goodforgoodbusiness.shared.encode.JSON;
import com.goodforgoodbusiness.webapp.ContentType;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import spark.Request;
import spark.Response;
import spark.Route;

@Singleton
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
	
	private final PointerCrypter crypter;
	
	@Inject
	public ShareRequestRoute(PointerCrypter pointerCrypter) {
		this.crypter = pointerCrypter;
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
		
		var shareKey = crypter.makeShareKey(keySpec, rangeSpec);
		
		return JSON.encode(
			new ShareResponse(keySpec, rangeSpec, shareKey)
		);
	}
}
