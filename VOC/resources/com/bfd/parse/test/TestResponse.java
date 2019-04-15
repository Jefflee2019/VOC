package com.bfd.parse.test;

import java.util.HashMap;
import java.util.Map;

import com.bfd.parse.util.JsonUtil;

public class TestResponse {
	private int code;
	private Map<String, Object> data;

	public TestResponse() {
	}

	public TestResponse(int code) {
		this.code = code;
	}

	public TestResponse(int code, Map<String, Object> data) {
		this.code = code;
		this.data = data;
	}

	public static TestResponse create() {
		return new TestResponse();
	}

	public static TestResponse create(int code, Map<String, Object> data) {
		return new TestResponse(code, data);
	}

	public static TestResponse create(int code) {
		return new TestResponse(code);
	}

	public TestResponse put(String key, Object val) {
		if (data == null) {
			data = new HashMap<String, Object>();
		}
		data.put(key, val);
		return this;
	}

	public TestResponse putAll(Map<String, Object> map) {
		if (map == null) {
			return this;
		}
		if (data == null) {
			data = new HashMap<String, Object>();
		}
		data.putAll(map);
		return this;
	}

	public String toJsonString() {
		if (data == null) {
			data = new HashMap<String, Object>();
			code = 1;
		}
		return JsonUtil.toJSONString(this);
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

}
