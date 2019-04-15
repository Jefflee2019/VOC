package com.bfd.parse.test.weibosinaparser;

import java.util.Map;

import com.bfd.crawler.utils.JsonUtils;

public class Task {

	
	private String url ;
	private String type;
	private String parentUserId;
	private String source;
	private Map<String, Object> taskdata;
	private Map<String, Object> spiderdata;
	private Map<String, Object> parsedata;
	
	
	
	
	public Map<String, Object> getParsedata() {
		return parsedata;
	}
	public void setParsedata(Map<String, Object> parserData) {
		this.parsedata = parserData;
	}
	public static Task createByPageData(Map pageMap){
		Task task = new Task();
		Map taskData = (Map)pageMap.get("taskdata");
		Map spiderData = (Map)pageMap.get("spiderdata");
		task.setTaskdata(taskData);
		task.setSpiderdata(spiderData);
		return task;
	}
	public Map<String, Object> getTaskdata() {
		return taskdata;
	}
	public void setTaskdata(Map<String, Object> taskData) {
		this.taskdata = taskData;
	}
	public Map<String, Object> getSpiderdata() {
		return spiderdata;
	}
	public void setSpiderdata(Map<String, Object> spiderData) {
		this.spiderdata = spiderData;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getParentUserId() {
		return parentUserId;
	}
	public void setParentUserId(String parentUserId) {
		this.parentUserId = parentUserId;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public static void main(String[] args) {
		Task t = new Task();
		System.out.println(JsonUtils.toJSONString(t));
	}
	
}
