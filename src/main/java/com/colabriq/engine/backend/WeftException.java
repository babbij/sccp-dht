package com.colabriq.engine.backend;

public class WeftException extends Exception {
	public WeftException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public WeftException(Throwable cause) {
		super(cause);
	}
}
