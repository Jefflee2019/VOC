package com.bfd.parse.json;

import java.util.Map;

/**
 * JSON数据解析结果Bean
 * 
 * @author yanhui.ji
 * 
 */
public class JsonParserResult {

	
	/**
	 * parsecode 解析结果：0 成功，5失败， 6 无解析插件
	 */
	private int parsecode;

	private Map<String, Object> data;

	public JsonParserResult() {
	}

	public JsonParserResult(int parsecode, Map<String, Object> data) {
		this.parsecode = parsecode;
		this.data = data;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public int getParsecode() {
		return parsecode;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	public void setParsecode(int parsecode) {
		this.parsecode = parsecode;
	}
}
