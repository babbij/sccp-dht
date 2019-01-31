package com.goodforgoodbusiness.engine.store.keys.spec;

import static com.goodforgoodbusiness.shared.TripleUtil.valueOf;

import java.util.Optional;

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
	
	public ShareKeySpec(Optional<String> sub, Optional<String> pre, Optional<String> obj) {
		this.subject = sub.orElse(null);
		this.predicate = pre.orElse(null);
		this.object = obj.orElse(null);
	}

	public ShareKeySpec(Triple triple) {
		this(
			valueOf(triple.getSubject()),
			valueOf(triple.getPredicate()),
			valueOf(triple.getObject())
		);
	}
	
	public Optional<String> getSubjectX() {
		return Optional.ofNullable(subject);
	}
	
	public Optional<String> getPredicate() {
		return Optional.ofNullable(predicate);
	}
	
	public Optional<String> getObject() {
		return Optional.ofNullable(object);
	}
	
	public String[] toValueArray() {
		return new String [] { subject, predicate, object };
	}
	
	@Override
	public int hashCode() {
		return
			getSubjectX().hashCode() ^ 
			getPredicate().hashCode() ^ 
			getObject().hashCode()
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
