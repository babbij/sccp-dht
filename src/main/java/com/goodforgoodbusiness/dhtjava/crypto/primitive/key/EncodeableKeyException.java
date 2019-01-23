package com.goodforgoodbusiness.dhtjava.crypto.primitive.key;

public class EncodeableKeyException extends Exception {
	EncodeableKeyException(String message) {
		super(message);
	}
	
	EncodeableKeyException(String message, Throwable cause) {
		super(message, cause);
	}
}
