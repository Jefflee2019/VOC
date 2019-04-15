package com.bfd.parse.entity;

import java.util.Date;

import com.bfd.crawler.utils.JacksonUtils;

public class ParsetemplateEntity extends BaseEntity{
    private Integer tmplid;

    private Integer siteid;

    private Integer pagetypeid;

    private String testurl;

    private String compareurl;

    private String required;

    private String output;

    private String dns;

    private String alias;

    private String tmpl;

    private Byte status;

    private Byte num;

    private Date createtime;

    private Date moditime;

    private String createuser;

    private String modiuser;

    private String rawdompath;

    public Integer getTmplid() {
        return tmplid;
    }

    public void setTmplid(Integer tmplid) {
        this.tmplid = tmplid;
    }

    public Integer getSiteid() {
        return siteid;
    }

    public void setSiteid(Integer siteid) {
        this.siteid = siteid;
    }

    public Integer getPagetypeid() {
        return pagetypeid;
    }

    public void setPagetypeid(Integer pagetypeid) {
        this.pagetypeid = pagetypeid;
    }

    public String getTesturl() {
        return testurl;
    }

    public void setTesturl(String testurl) {
        this.testurl = testurl == null ? null : testurl.trim();
    }

    public String getCompareurl() {
        return compareurl;
    }

    public void setCompareurl(String compareurl) {
        this.compareurl = compareurl == null ? null : compareurl.trim();
    }

    public String getRequired() {
        return required;
    }

    public void setRequired(String required) {
        this.required = required == null ? null : required.trim();
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output == null ? null : output.trim();
    }

    public String getDns() {
        return dns;
    }

    public void setDns(String dns) {
        this.dns = dns == null ? null : dns.trim();
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias == null ? null : alias.trim();
    }

    public String getTmpl() {
        return tmpl;
    }

    public void setTmpl(String tmpl) {
        this.tmpl = tmpl == null ? null : tmpl.trim();
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Byte getNum() {
        return num;
    }

    public void setNum(Byte num) {
        this.num = num;
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

    public String getRawdompath() {
        return rawdompath;
    }

    public void setRawdompath(String rawdompath) {
        this.rawdompath = rawdompath == null ? null : rawdompath.trim();
    }
    
    public static ParsetemplateEntity fromMap(String json){
    	return JacksonUtils.extractObject(json, ParsetemplateEntity.class);
    }

	@Override
	public String getCacheKey() {
		return this.siteid+"|"+this.pagetypeid;
	}
}