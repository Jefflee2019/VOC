package com.bfd.parse.entity;

import java.util.Date;

public class SitepageconfigEntity extends BaseEntity{
    private Integer sitepageid;

    private Integer siteid;

    private Integer pagetypeid;

    private Byte priority;

    private String iidregex;

    private String iidtesturl;

    private Byte needlogin;

    private Byte needajaxdata;

    private Byte needtemplate;

    private Byte preprocesstag;

    private Byte reprocesstag;

    private Byte jsonprocesstag;

    private Byte fromattr;

    private Byte status;

    private Date createtime;

    private Date moditime;

    private String createuser;

    private String modiuser;
    
    private Byte iidtag;
    
    private String datatype;
    
    private Byte urlrule;
    
    private String detailrule;
    
    private Byte updatecookie;
    
    
    
    


	public Byte getUpdatecookie() {
		return updatecookie;
	}

	public void setUpdatecookie(Byte updatecookie) {
		this.updatecookie = updatecookie;
	}

	public String getDatatype() {
		return datatype;
	}

	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}

	public Byte getIidtag() {
		return iidtag;
	}

	public void setIidtag(Byte iidtag) {
		this.iidtag = iidtag;
	}

	public Integer getSitepageid() {
        return sitepageid;
    }

    public void setSitepageid(Integer sitepageid) {
        this.sitepageid = sitepageid;
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

    public Byte getPriority() {
        return priority;
    }

    public void setPriority(Byte priority) {
        this.priority = priority;
    }

    public String getIidregex() {
        return iidregex;
    }

    public void setIidregex(String iidregex) {
        this.iidregex = iidregex == null ? null : iidregex.trim();
    }

    public String getIidtesturl() {
        return iidtesturl;
    }

    public void setIidtesturl(String iidtesturl) {
        this.iidtesturl = iidtesturl == null ? null : iidtesturl.trim();
    }

    public Byte getNeedlogin() {
        return needlogin;
    }

    public void setNeedlogin(Byte needlogin) {
        this.needlogin = needlogin;
    }

    public Byte getNeedajaxdata() {
        return needajaxdata;
    }

    public void setNeedajaxdata(Byte needajaxdata) {
        this.needajaxdata = needajaxdata;
    }

    public Byte getNeedtemplate() {
        return needtemplate;
    }

    public void setNeedtemplate(Byte needtemplate) {
        this.needtemplate = needtemplate;
    }

    public Byte getPreprocesstag() {
        return preprocesstag;
    }

    public void setPreprocesstag(Byte preprocesstag) {
        this.preprocesstag = preprocesstag;
    }

    public Byte getReprocesstag() {
        return reprocesstag;
    }

    public void setReprocesstag(Byte reprocesstag) {
        this.reprocesstag = reprocesstag;
    }

    public Byte getJsonprocesstag() {
        return jsonprocesstag;
    }

    public void setJsonprocesstag(Byte jsonprocesstag) {
        this.jsonprocesstag = jsonprocesstag;
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
		return this.siteid+"|"+this.pagetypeid;
	}

	public Byte getUrlrule() {
		return urlrule;
	}

	public void setUrlrule(Byte urlrule) {
		this.urlrule = urlrule;
	}

	public String getDetailrule() {
		return detailrule;
	}

	public void setDetailrule(String detailrule) {
		this.detailrule = detailrule;
	}
	
	
}