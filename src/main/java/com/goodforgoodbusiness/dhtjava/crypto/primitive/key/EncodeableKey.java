package com.goodforgoodbusiness.dhtjava.crypto.primitive.key;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.security.Key;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.JsonAdapter;

@JsonAdapter(EncodeableKey.Serializer.class)
public interface EncodeableKey extends Key {
	public static class Serializer implements JsonSerializer<EncodeableKey>, JsonDeserializer<EncodeableKey> {
		@Override
		public JsonElement serialize(EncodeableKey obj, Type type, JsonSerializationContext ctx) {
			return new JsonPrimitive(obj.toEncodedString());
		}
		
		@Override
		public EncodeableKey deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) throws JsonParseException {
			try {
				// this should work for derived classes as well.
				return (EncodeableKey)((Class<?>)type).getConstructor(String.class).newInstance(json.getAsString());
			}
			catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
				throw new JsonParseException(e);
			}
		}
	}
	
	public String toEncodedString();
}
