package com.goodforgoodbusiness.dhtjava.keys;

import static com.goodforgoodbusiness.shared.TripleUtil.valueOf;

import org.apache.jena.graph.Triple;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class KeyIndex {
	@Expose
	@SerializedName("subject")
	
	private final String subject;
	
	@Expose
	@SerializedName("predicate")
	private final String predicate;
	
	@Expose
	@SerializedName("object")
	private final String object;
	
	public KeyIndex(Triple triple) {
		this.subject = valueOf(triple.getSubject());
		this.predicate = valueOf(triple.getPredicate());
		this.object = valueOf(triple.getObject());
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
		
		if (!(o instanceof KeyIndex)) {
			return false;
		}
		
		KeyIndex other = (KeyIndex)o;
		
		return
			((subject != null) ? subject.equals(other.subject) : (other.subject == null)) && 
			((predicate != null) ? predicate.equals(other.predicate) : (other.predicate == null)) && 
			((object != null) ? object.equals(other.object) : (other.object == null))
		;
		
	}
}
