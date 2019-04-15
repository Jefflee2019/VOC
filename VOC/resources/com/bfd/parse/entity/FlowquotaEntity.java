package com.bfd.parse.entity;

import java.math.BigDecimal;
import java.util.Date;

import com.bfd.parse.entity.BaseEntity;

public class FlowquotaEntity extends BaseEntity{
    private Integer quotaid;

    private Integer siteid;

    private Integer concurrency;

    private Integer quota;

    private Short intv;

    private Integer timeout;

    private Integer total;

    private BigDecimal upthreshold;

    private BigDecimal upstep;

    private BigDecimal uplimit;

    private BigDecimal downthreshold;

    private BigDecimal downstep;

    private BigDecimal downlimit;

    private Byte status;

    private String memo;

    private Date createtime;

    private Date moditime;

    private String createuser;

    private String modiuser;

    private String iplist;

    public Integer getQuotaid() {
        return quotaid;
    }

    public void setQuotaid(Integer quotaid) {
        this.quotaid = quotaid;
    }

    public Integer getSiteid() {
        return siteid;
    }

    public void setSiteid(Integer siteid) {
        this.siteid = siteid;
    }

    public Integer getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(Integer concurrency) {
        this.concurrency = concurrency;
    }

    public Integer getQuota() {
        return quota;
    }

    public void setQuota(Integer quota) {
        this.quota = quota;
    }

    public Short getIntv() {
        return intv;
    }

    public void setIntv(Short intv) {
        this.intv = intv;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public BigDecimal getUpthreshold() {
        return upthreshold;
    }

    public void setUpthreshold(BigDecimal upthreshold) {
        this.upthreshold = upthreshold;
    }

    public BigDecimal getUpstep() {
        return upstep;
    }

    public void setUpstep(BigDecimal upstep) {
        this.upstep = upstep;
    }

    public BigDecimal getUplimit() {
        return uplimit;
    }

    public void setUplimit(BigDecimal uplimit) {
        this.uplimit = uplimit;
    }

    public BigDecimal getDownthreshold() {
        return downthreshold;
    }

    public void setDownthreshold(BigDecimal downthreshold) {
        this.downthreshold = downthreshold;
    }

    public BigDecimal getDownstep() {
        return downstep;
    }

    public void setDownstep(BigDecimal downstep) {
        this.downstep = downstep;
    }

    public BigDecimal getDownlimit() {
        return downlimit;
    }

    public void setDownlimit(BigDecimal downlimit) {
        this.downlimit = downlimit;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo == null ? null : memo.trim();
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

    public String getIplist() {
        return iplist;
    }

    public void setIplist(String iplist) {
        this.iplist = iplist == null ? null : iplist.trim();
    }

	@Override
	public String getCacheKey() {
		return this.getSiteid()+"";
	}
}