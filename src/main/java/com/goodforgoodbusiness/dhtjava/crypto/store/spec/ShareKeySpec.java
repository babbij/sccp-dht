package com.goodforgoodbusiness.dhtjava.crypto.store.spec;

import static com.goodforgoodbusiness.shared.TripleUtil.valueOf;

import org.apache.jena.graph.Triple;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The specification for a ShareKey, the sub/pre/obj that is required.
 */
public class ShareKeySpec {
	@Expose
	@SerializedName("sub")
	private String subject;
	
	@Expose
	@SerializedName("pre")
	private String predicate;
	
	@Expose
	@SerializedName("obj")
	private String object;
	
	public ShareKeySpec(String sub, String pre, String obj) {
		this.subject = sub;
		this.predicate = pre;
		this.object = obj;
	}

	public ShareKeySpec(Triple triple) {
		this(
			valueOf(triple.getSubject()),
			valueOf(triple.getPredicate()),
			valueOf(triple.getObject())
		);
	}
	
	public String getSubject() {
		return subject;
	}
	
	public String getPredicate() {
		return predicate;
	}
	
	public String getObject() {
		return object;
	}
	
	@Override
	public int hashCode() {
		return
			((subject != null) ? subject.hashCode() : 0) ^ 
			((predicate != null) ? predicate.hashCode() : 0) ^ 
			((object != null) ? object.hashCode() : 0)
		;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		
		if (!(o instanceof ShareKeySpec)) {
			return false;
		}
		
		ShareKeySpec other = (ShareKeySpec)o;
		
		return
			((subject != null) ? subject.equals(other.subject) : (other.subject == null)) && 
			((predicate != null) ? predicate.equals(other.predicate) : (other.predicate == null)) && 
			((object != null) ? object.equals(other.object) : (other.object == null))
		;
		
	}
	
	@Override
	public String toString() {
		return "ShareKeySpec(" + subject + ", " + predicate + ", " + object + ")";
	}
}
