package com.gummagames.payments;

/**
 * Runtime exception to indicate various market public keys are not properly initialized
 * 
 * @author Chris Cole
 *
 */
@SuppressWarnings("serial")
public class KeyInitializationException extends RuntimeException {
	
	private String message;
	
	private Throwable cause;
	
	public Throwable getCause() {
		return cause;
	}

	public void setCause(Throwable cause) {
		this.cause = cause;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public KeyInitializationException (){}
	
	public KeyInitializationException (String message){
		setMessage(message);
	}

	public KeyInitializationException (String message, Throwable throwable){
		setMessage(message);
		setCause(throwable);
	}

}
