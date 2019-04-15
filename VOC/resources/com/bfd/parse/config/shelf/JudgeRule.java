package com.bfd.parse.config.shelf;

import java.util.Map;

import com.bfd.parse.Constants;
//TODO:这期不需要做关于上下架的处理
public class JudgeRule {

	private Integer id;
	private String bid;
	private String pageType;
	private String regex;
	private String type;
	private String extend;
	private String status;
	private static String[] flds = { "type", "regex" };

	public static JudgeRule create(Map<String, Object> param) {

		for (String key : flds) {
			if (!param.containsKey(key)) {
				return null;
			}
		}

		Integer id = (Integer) param.get("id");
		String bid = (String) param.get(Constants.cid);
		String type = (String) param.get("type");
		String regex = (String) param.get("regex");
		String pageType = (String) param.get("pagetype");
		String extend = "";
		if (param.containsKey("extend")) {
			extend = (String) param.get("extend");
		}
		String status = "0";
		if (param.containsKey("status")) {
			status = (String) param.get("status");
		}
		return new JudgeRule(id, bid, pageType, type, regex, extend, status);
	}

	public JudgeRule(Integer id, String bid, String pageType, String type, String regex, String extend, String status) {
		this.id = id;
		this.bid = bid;
		this.pageType = pageType;
		this.type = type;
		this.regex = regex;
		this.status = status;
		this.extend = extend;
	}

	public JudgeRule() {
	}

	public String getBid() {
		return bid;
	}

	public void setBid(String bid) {
		this.bid = bid;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getExtend() {
		return extend;
	}

	public void setExtend(String extend) {
		this.extend = extend;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getPageType() {
		return pageType;
	}

	public void setPageType(String pageType) {
		this.pageType = pageType;
	}
}
