package org.generationcp.bms.context;

public class ContextResolutionException extends RuntimeException {
	
	private static final long serialVersionUID = -2207676473874258902L;

	public ContextResolutionException(String message) {
		super(message);
	}
	
	public ContextResolutionException(String message, Throwable cause) {
		super(message, cause);
	}

}
