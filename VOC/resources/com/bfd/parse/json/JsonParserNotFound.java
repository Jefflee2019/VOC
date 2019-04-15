package com.bfd.parse.json;

@SuppressWarnings("serial")
public class JsonParserNotFound extends JsonParserException {

	private String url;

	public JsonParserNotFound(String url) {
		this(url, "json parser not found for url=" + url);
	}

	public JsonParserNotFound(String url, String message) {
		super(message);
		this.url = url;
	}

	public String getUrl() {
		return url;
	}
}
