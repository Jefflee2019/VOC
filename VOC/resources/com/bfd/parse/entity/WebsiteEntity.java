package com.bfd.parse.entity;

import java.util.Date;

public class WebsiteEntity extends BaseEntity{
    private Integer siteid;

    private String cid;

    private String entry;

    private Byte listdetect;

    private Integer projectid;

    private Byte status;

    private Byte offtag;

    private Byte deathchecktag;

    private Byte imgdownload;

    private Byte restore;

    private Byte iidtag;

    private String description;

    private Byte timetag;

    private String timerange;

    private String comment;

    private Byte learnstatus;

    private Date lastlistdetect;

    private Date createtime;

    private Date moditime;

    private String createuser;

    private String modiuser;
    
    private Byte sitetype;
    
    

    public Byte getSitetype() {
		return sitetype;
	}

	public void setSitetype(Byte sitetype) {
		this.sitetype = sitetype;
	}

	public Integer getSiteid() {
        return siteid;
    }

    public void setSiteid(Integer siteid) {
        this.siteid = siteid;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid == null ? null : cid.trim();
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry == null ? null : entry.trim();
    }

    public Byte getListdetect() {
        return listdetect;
    }

    public void setListdetect(Byte listdetect) {
        this.listdetect = listdetect;
    }

    public Integer getProjectid() {
        return projectid;
    }

    public void setProjectid(Integer projectid) {
        this.projectid = projectid;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Byte getOfftag() {
        return offtag;
    }

    public void setOfftag(Byte offtag) {
        this.offtag = offtag;
    }

    public Byte getDeathchecktag() {
        return deathchecktag;
    }

    public void setDeathchecktag(Byte deathchecktag) {
        this.deathchecktag = deathchecktag;
    }

    public Byte getImgdownload() {
        return imgdownload;
    }

    public void setImgdownload(Byte imgdownload) {
        this.imgdownload = imgdownload;
    }

    public Byte getRestore() {
        return restore;
    }

    public void setRestore(Byte restore) {
        this.restore = restore;
    }

    public Byte getIidtag() {
        return iidtag;
    }

    public void setIidtag(Byte iidtag) {
        this.iidtag = iidtag;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public Byte getTimetag() {
        return timetag;
    }

    public void setTimetag(Byte timetag) {
        this.timetag = timetag;
    }

    public String getTimerange() {
        return timerange;
    }

    public void setTimerange(String timerange) {
        this.timerange = timerange == null ? null : timerange.trim();
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment == null ? null : comment.trim();
    }

    public Byte getLearnstatus() {
        return learnstatus;
    }

    public void setLearnstatus(Byte learnstatus) {
        this.learnstatus = learnstatus;
    }

    public Date getLastlistdetect() {
        return lastlistdetect;
    }

    public void setLastlistdetect(Date lastlistdetect) {
        this.lastlistdetect = lastlistdetect;
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
		return this.siteid+"";
	}
}