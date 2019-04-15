package com.bfd.parse.reprocess;

public class ReProcessorNotFound extends Exception {

	private String pluginId;

	public ReProcessorNotFound(String url) {
		this(url, "reprocessor not found for pluginId=" + url);
	}

	public ReProcessorNotFound(String url, String message) {
		super(message);
		this.pluginId = url;
	}

	public String getPluginId() {
		return pluginId;
	}

}
