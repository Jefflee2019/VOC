package com.bfd.parse.entity;

import java.util.Date;

public class FielddefineEntity extends BaseEntity{
    private Integer fieldid;

    private Integer pagetypeid;

    private String fieldnamecn;

    private String fieldnameen;

    private Byte fieldtype;

    private Integer valuerange0;

    private Integer valuerange1;

    private Byte iskeyfield;

    private Byte ismulti;

    private Integer linktype;

    private Byte createtask;

    private Byte fromattr;

    private Byte status;

    private Date createtime;

    private Date moditime;

    private String createuser;

    private String modiuser;
    
    private String defaultfield;
    
    

    public String getDefaultfield() {
		return defaultfield;
	}

	public void setDefaultfield(String defaultfield) {
		this.defaultfield = defaultfield;
	}

	public Integer getFieldid() {
        return fieldid;
    }

    public void setFieldid(Integer fieldid) {
        this.fieldid = fieldid;
    }

    public Integer getPagetypeid() {
        return pagetypeid;
    }

    public void setPagetypeid(Integer pagetypeid) {
        this.pagetypeid = pagetypeid;
    }

    public String getFieldnamecn() {
        return fieldnamecn;
    }

    public void setFieldnamecn(String fieldnamecn) {
        this.fieldnamecn = fieldnamecn == null ? null : fieldnamecn.trim();
    }

    public String getFieldnameen() {
        return fieldnameen;
    }

    public void setFieldnameen(String fieldnameen) {
        this.fieldnameen = fieldnameen == null ? null : fieldnameen.trim();
    }

    public Byte getFieldtype() {
        return fieldtype;
    }

    public void setFieldtype(Byte fieldtype) {
        this.fieldtype = fieldtype;
    }

    public Integer getValuerange0() {
        return valuerange0;
    }

    public void setValuerange0(Integer valuerange0) {
        this.valuerange0 = valuerange0;
    }

    public Integer getValuerange1() {
        return valuerange1;
    }

    public void setValuerange1(Integer valuerange1) {
        this.valuerange1 = valuerange1;
    }

    public Byte getIskeyfield() {
        return iskeyfield;
    }

    public void setIskeyfield(Byte iskeyfield) {
        this.iskeyfield = iskeyfield;
    }

    public Byte getIsmulti() {
        return ismulti;
    }

    public void setIsmulti(Byte ismulti) {
        this.ismulti = ismulti;
    }

    public Integer getLinktype() {
        return linktype;
    }

    public void setLinktype(Integer linktype) {
        this.linktype = linktype;
    }

    public Byte getCreatetask() {
        return createtask;
    }

    public void setCreatetask(Byte createtask) {
        this.createtask = createtask;
    }

    public Byte getFromattr() {
        return fromattr;
    }

    public void setFromattr(Byte fromattr) {
        this.fromattr = fromattr;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
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
		return this.pagetypeid+"";
	}
}