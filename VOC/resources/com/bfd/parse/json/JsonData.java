package com.bfd.parse.json;

import java.util.Map;

/**
 * @author yanhui.ji
 */
public class JsonData {

	private String url; // url
	private byte[] data;
	private String charset; // 数据编码
	private String httpcode; // 下载结果状态码
	private Map<String, Object> extend; // 扩展数据

	public JsonData() {
	}

	public JsonData(String url, byte[] data, String charset, String httpcode, Map<String, Object> extend) {
		this.url = url;
		this.data = data;
		this.charset = charset;
		this.httpcode = httpcode;
		this.extend = extend;
	}

	public byte[] getData() {
		return data;
	}

	public String getUrl() {
		return url;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getCharset() {
		return charset;
	}

	public String getHttpcode() {
		return httpcode;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public void setHttpcode(String httpcode) {
		this.httpcode = httpcode;
	}

	public Map<String, Object> getExtend() {
		return extend;
	}

	public void setExtend(Map<String, Object> extend) {
		this.extend = extend;
	}

	public boolean downloadSuccess() {
		if ("0".equals(this.httpcode)) {
			return true;
		}
		return false;
	}
}