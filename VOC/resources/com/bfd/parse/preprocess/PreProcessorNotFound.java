package com.bfd.parse.preprocess;

public class PreProcessorNotFound extends Exception {

	private String pluginId;

	public PreProcessorNotFound(String url) {
		this(url, "reprocessor not found for pluginId=" + url);
	}

	public PreProcessorNotFound(String url, String message) {
		super(message);
		this.pluginId = url;
	}

	public String getPluginId() {
		return pluginId;
	}

}
