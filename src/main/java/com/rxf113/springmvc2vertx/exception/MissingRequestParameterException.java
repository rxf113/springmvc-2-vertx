package com.rxf113.springmvc2vertx.exception;

public class MissingRequestParameterException extends RuntimeException {

	private final String parameterName;

	private final String parameterType;


	/**
	 * Constructor for MissingServletRequestParameterException.
	 * @param parameterName the name of the missing parameter
	 * @param parameterType the expected type of the missing parameter
	 */
	public MissingRequestParameterException(String parameterName, String parameterType) {
		super("");
		this.parameterName = parameterName;
		this.parameterType = parameterType;
	}


	@Override
	public String getMessage() {
		return "Required " + this.parameterType + " parameter '" + this.parameterName + "' is not present";
	}

}
