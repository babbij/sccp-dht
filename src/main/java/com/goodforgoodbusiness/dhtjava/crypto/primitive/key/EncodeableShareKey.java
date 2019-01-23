package com.goodforgoodbusiness.dhtjava.crypto.primitive.key;

import java.lang.reflect.Type;
import java.security.KeyPair;

import com.goodforgoodbusiness.kpabe.key.KPABEPublicKey;
import com.goodforgoodbusiness.kpabe.key.KPABEShareKey;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

@JsonAdapter(EncodeableShareKey.Serializer.class)
public final class EncodeableShareKey {
	public static class Serializer implements JsonSerializer<EncodeableShareKey>, JsonDeserializer<EncodeableShareKey> {
		@Override
		public JsonElement serialize(EncodeableShareKey obj, Type type, JsonSerializationContext ctx) {
			JsonObject o = new JsonObject();
			o.addProperty("public", obj.publicKey.toString());
			o.addProperty("share", obj.shareKey.toString());
			return o;
		}

		@Override
		public EncodeableShareKey deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) throws JsonParseException {
			return new EncodeableShareKey(
				new KPABEPublicKey(json.getAsJsonObject().get("public").getAsString()),
				new KPABEShareKey(json.getAsJsonObject().get("share").getAsString())
			);
		}
		
	}
	
	@Expose
	@SerializedName("public")
	private KPABEPublicKey publicKey;
	
	@Expose
	@SerializedName("share")
	private KPABEShareKey shareKey;
	
	public EncodeableShareKey(KPABEPublicKey publicKey, KPABEShareKey shareKey) {
		this.publicKey = publicKey;
		this.shareKey = shareKey;
	}
	
	public EncodeableShareKey(KeyPair keyPair) {
		this(
			(KPABEPublicKey)keyPair.getPublic(),
			(KPABEShareKey)keyPair.getPrivate()
		);
	}

	public KPABEPublicKey getPublic() {
		return publicKey;
	}

	public KPABEShareKey getShare() {
		return shareKey;
	}
	
	public KeyPair toKeyPair() {
		return new KeyPair(publicKey, shareKey);
	}

	@Override
	public int hashCode() {
		return shareKey.hashCode() ^ publicKey.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		
		if (o instanceof EncodeableKeyPair) {
			return
				publicKey.equals(((EncodeableShareKey)o).publicKey) &&
				shareKey.equals(((EncodeableShareKey)o).shareKey)
			;
		}
		
		if (o instanceof KeyPair) {
			return
				publicKey.equals(((KeyPair)o).getPublic()) &&
				shareKey.equals(((KeyPair)o).getPrivate())
			;
		}
		
		return false;
	}
}
