package com.bfd.parse.config.iid;

import java.util.Map;
import java.util.regex.Pattern;

public class ParseCfg {
	private Integer id;
	private String cid;
	private String type;
	private String iidRegex;
	private Integer status;
	private String createtime;
	private Pattern iidPattern;

	public ParseCfg() {
	}

	public ParseCfg(Integer id, String cid, String type, String iidRegex, Integer status) {
		this.id = id;
		this.cid = cid;
		this.type = type;
		this.iidRegex = iidRegex;
		this.status = status;
	}

	public static ParseCfg fromMap(Map<String, Object> param) {
		Integer id = (Integer) param.get("id");
		String cid = (String) param.get("cid");
		String type = (String) param.get("type");
		String iidRegex = (String) param.get("iidRegex");
		Integer status = (Integer) param.get("status");
		return new ParseCfg(id, cid, type, iidRegex, status);
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getIidRegex() {
		return iidRegex;
	}

	public void setIidRegex(String iidRegex) {
		this.iidRegex = iidRegex;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getCreatetime() {
		return createtime;
	}

	public void setCreatetime(String createtime) {
		this.createtime = createtime;
	}

	public Pattern getIidPattern() {
		if (iidPattern == null) {
			iidPattern = Pattern.compile(this.iidRegex);
		}
		return iidPattern;
	}

	public void setIidPattern(Pattern iidPattern) {
		this.iidPattern = iidPattern;
	}

}
