package com.bfd.parse.entity;

import java.util.Date;

public class ECConfigEntity extends BaseEntity{

	private String cid;
	private String iscontain;
	private Byte isdelete;
	private String redirecturl;
	private Integer nextpagetime;
	private Byte crawlcomment;
	private Date moditime;
	public String getCid() {
		return cid;
	}
	public void setCid(String cid) {
		this.cid = cid;
	}
	public String getIscontain() {
		return iscontain;
	}
	public void setIscontain(String iscontain) {
		this.iscontain = iscontain;
	}
	public Byte getIsdelete() {
		return isdelete;
	}
	public void setIsdelete(Byte isdelete) {
		this.isdelete = isdelete;
	}
	public String getRedirecturl() {
		return redirecturl;
	}
	public void setRedirecturl(String redirecturl) {
		this.redirecturl = redirecturl;
	}
	public Integer getNextpagetime() {
		return nextpagetime;
	}
	public void setNextpagetime(Integer nextpagetime) {
		this.nextpagetime = nextpagetime;
	}
	public Byte getCrawlcomment() {
		return crawlcomment;
	}
	public void setCrawlcomment(Byte crawlcomment) {
		this.crawlcomment = crawlcomment;
	}
	public Date getModitime() {
		return moditime;
	}
	public void setModitime(Date moditime) {
		this.moditime = moditime;
	}
	@Override
	public String getCacheKey() {
		return this.cid;
	} 

}
