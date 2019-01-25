package com.goodforgoodbusiness.engine.store.keys.spec;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

/**
 * Time range for a ShareKey.
 * Either can be null.
 */
@JsonAdapter(ShareRangeSpec.Serializer.class)
public class ShareRangeSpec {
	public static class Serializer implements JsonSerializer<ShareRangeSpec>, JsonDeserializer<ShareRangeSpec> {
		@Override
		public ShareRangeSpec deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) {
			var o = json.getAsJsonObject();
			return new ShareRangeSpec(
				o.get("start").getAsString(), o.get("end").getAsString()
			);
		}

		@Override
		public JsonElement serialize(ShareRangeSpec spec, Type type, JsonSerializationContext ctx) {
			JsonObject o = new JsonObject();
			o.addProperty("start", (spec.start != null) ? spec.start.toLocalDateTime().toString() : null);
			o.addProperty("end", (spec.end != null) ? spec.end.toLocalDateTime().toString() : null);
			return o;
		}
	}
	
	@Expose
	@SerializedName("start")
	private ZonedDateTime start;
	
	@Expose
	@SerializedName("end")
	private ZonedDateTime end;
	
	public ShareRangeSpec(String start, String end) {
		if (start != null) {
			this.start = LocalDateTime.parse(start, ISO_LOCAL_DATE_TIME).atZone(ZoneId.of("UTC"));
		}
		else {
			this.start = null;
		}
		
		if (end != null) {
			this.end = LocalDateTime.parse(end, ISO_LOCAL_DATE_TIME).atZone(ZoneId.of("UTC"));
		}
		else {
			this.end = null;
		}
	}
	
	public ShareRangeSpec(ZonedDateTime start, ZonedDateTime end) {
		this.start = start;
		this.end = end;
	}
	
	public ZonedDateTime getStart() {
		return start;
	}
	
	public ZonedDateTime getEnd() {
		return end;
	}
	
	@Override
	public int hashCode() {
		return ((start != null) ? start.hashCode() : 0) ^ ((end != null) ? end.hashCode() : 0);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		
		if (!(o instanceof ShareRangeSpec)) {
			return false;
		}
		
		ShareRangeSpec other = (ShareRangeSpec)o;
		
		return
			((start != null) ? start.equals(other.start) : (other.start == null)) && 
			((end != null) ? end.equals(other.end) : (other.end == null)) 
		;
		
	}
	
	@Override
	public String toString() {
		return "ShareRangeSpec(" + start + ", " + end + ")";
	}
}
