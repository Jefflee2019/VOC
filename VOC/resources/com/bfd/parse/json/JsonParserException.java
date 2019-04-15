package com.bfd.parse.json;

@SuppressWarnings("serial")
public class JsonParserException extends Exception {

	public JsonParserException() {
		super();
	}

	public JsonParserException(String message) {
		super(message);
	}

	public JsonParserException(String message, Throwable cause) {
		super(message, cause);
	}

	public JsonParserException(Throwable cause) {
		super(cause);
	}

}
