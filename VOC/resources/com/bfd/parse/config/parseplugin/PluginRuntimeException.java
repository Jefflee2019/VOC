package com.bfd.parse.config.parseplugin;

public class PluginRuntimeException extends Exception {

	private static final long serialVersionUID = 1L;

	public PluginRuntimeException(Throwable cause) {
		super(cause);
	}

	public PluginRuntimeException(String message) {
		super(message);
	}
}
