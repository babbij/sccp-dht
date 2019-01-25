package com.goodforgoodbusiness.engine.crypto.primitive.key;

public class EncodeableKeyException extends Exception {
	EncodeableKeyException(String message) {
		super(message);
	}
	
	EncodeableKeyException(String message, Throwable cause) {
		super(message, cause);
	}
}
