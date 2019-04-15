package com.bfd.parse.entity;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
@JsonIgnoreProperties({"cacheKey"})
public abstract class BaseEntity {
	@JsonIgnore
	protected BaseEntity next;
	
	public BaseEntity getNext() {
		return next;
	}
//	public void addSameCacheKeyObj(BaseEntity next){
//		getLastEntity(this).setNext(next);
//	}
	public void setNext(BaseEntity next) {
		this.next = next;
	}

	public void addEntityToLast(BaseEntity newObj){
		BaseEntity rs = this;
		while(rs.getNext()!=null){
			rs = rs.getNext();
		}
		rs.setNext(newObj);
	}
	
	public abstract String getCacheKey();
}
