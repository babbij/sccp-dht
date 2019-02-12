package com.goodforgoodbusiness.engine.route;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.engine.crypto.ShareKeyCreator;
import com.goodforgoodbusiness.engine.crypto.primitive.key.EncodeableShareKey;
import com.goodforgoodbusiness.model.TriTuple;
import com.goodforgoodbusiness.shared.encode.JSON;
import com.goodforgoodbusiness.webapp.ContentType;
import com.goodforgoodbusiness.webapp.error.BadRequestException;
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
		private TriTuple pattern;

		@Expose
		@SerializedName("start")
		private ZonedDateTime start;
		
		@Expose
		@SerializedName("end")
		private ZonedDateTime end;
		
		@Expose
		@SerializedName("key")
		private EncodeableShareKey key;
		
		public ShareResponse(TriTuple pattern, Optional<ZonedDateTime> start, Optional<ZonedDateTime> end, EncodeableShareKey key) {
			this.key = key;
			this.pattern = pattern;
			this.start = start.orElse(null);
			this.end = end.orElse(null);
		}
	}
	
	private final ShareKeyCreator shareKeyCreator;
	
	@Inject
	public ShareRequestRoute(ShareKeyCreator shareKeyCreator) {
		this.shareKeyCreator = shareKeyCreator;
	}
	
	@Override
	public Object handle(Request req, Response res) throws Exception {
		res.type(ContentType.json.getContentTypeString());
		
		var pattern = new TriTuple(
			Optional.ofNullable(req.queryParams("sub")),
			Optional.ofNullable(req.queryParams("pre")),
			Optional.ofNullable(req.queryParams("obj"))
		);
		
//		if (!pattern.getSubject().isPresent() && !pattern.getObject().isPresent()) {
//			throw new BadRequestException("Must specify at least subject and object");
//		}
		
		var start = getDateTime(req.queryParams("start"));
		var end = getDateTime(req.queryParams("end"));
		
		log.info("Creating share key for request on " + pattern);
		
		return JSON.encode(
			new ShareResponse(
				pattern, 
				start,
				end, 
				shareKeyCreator.newKey(pattern, start, end)
			)
		);
	}
	
	private static Optional<ZonedDateTime> getDateTime(String input) throws BadRequestException {
		try {
			return Optional
				.ofNullable(input)
				.map(i -> LocalDateTime.parse(i, ISO_LOCAL_DATE_TIME).atZone(ZoneId.of("UTC")))
			;
		}
		catch (DateTimeParseException e) {
			throw new BadRequestException("Bad datetime", e);
		}
	}
}
