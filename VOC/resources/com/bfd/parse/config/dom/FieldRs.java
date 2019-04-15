package com.bfd.parse.config.dom;

public class FieldRs {
	private String link;// 链接
	private String linkType;// 链接类型
	private String text;// 文本内容

	private String rawlink;
	private String nodeName;
	private String createTask;
	
	

	public String getCreateTask() {
		return createTask;
	}

	public void setCreateTask(String createTask) {
		this.createTask = createTask;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getLinkType() {
		return linkType;
	}

	public void setLinkType(String linkType) {
		this.linkType = linkType;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getRawlink() {
		return rawlink;
	}

	public void setRawlink(String rawlink) {
		this.rawlink = rawlink;
	}
	
}
