package com.bfd.parse.config;

import java.util.Map;

import com.bfd.parse.entity.BaseEntity;

public interface Config {
	public static final String REQUEST_ALL = "{\"type\":\"ALL\"}";;
	boolean requestConfig();

	String name();
	Map getData();
}
