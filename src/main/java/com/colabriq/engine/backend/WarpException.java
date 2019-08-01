package com.colabriq.engine.backend;

public class WarpException extends Exception {
	public WarpException(String message, Throwable cause) {
		super(message, cause);
	}

	public WarpException(Throwable cause) {
		super(cause);
	}
}
