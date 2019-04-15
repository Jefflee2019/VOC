package com.bfd.parse.entity;

import java.util.Date;

public class PagedefineEntity extends BaseEntity{
    private Integer pagetypeid;

    private Byte category;

    private String pagenamecn;

    private String pagenameen;

    private Byte priority;

    private Byte status;

    private Byte parsemethod;

    private Date createtime;

    private Date moditime;

    private String createuser;

    private String modiuser;

    public Integer getPagetypeid() {
        return pagetypeid;
    }

    public void setPagetypeid(Integer pagetypeid) {
        this.pagetypeid = pagetypeid;
    }

    public Byte getCategory() {
        return category;
    }

    public void setCategory(Byte category) {
        this.category = category;
    }

    public String getPagenamecn() {
        return pagenamecn;
    }

    public void setPagenamecn(String pagenamecn) {
        this.pagenamecn = pagenamecn == null ? null : pagenamecn.trim();
    }

    public String getPagenameen() {
        return pagenameen;
    }

    public void setPagenameen(String pagenameen) {
        this.pagenameen = pagenameen == null ? null : pagenameen.trim();
    }

    public Byte getPriority() {
        return priority;
    }

    public void setPriority(Byte priority) {
        this.priority = priority;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Byte getParsemethod() {
        return parsemethod;
    }

    public void setParsemethod(Byte parsemethod) {
        this.parsemethod = parsemethod;
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public Date getModitime() {
        return moditime;
    }

    public void setModitime(Date moditime) {
        this.moditime = moditime;
    }

    public String getCreateuser() {
        return createuser;
    }

    public void setCreateuser(String createuser) {
        this.createuser = createuser == null ? null : createuser.trim();
    }

    public String getModiuser() {
        return modiuser;
    }

    public void setModiuser(String modiuser) {
        this.modiuser = modiuser == null ? null : modiuser.trim();
    }

	@Override
	public String getCacheKey() {
		return this.getPagetypeid()+"";
	}
}