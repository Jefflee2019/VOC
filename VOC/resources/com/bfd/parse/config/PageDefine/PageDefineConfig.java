package com.bfd.parse.config.PageDefine;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.JacksonUtils;
import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.config.AConfig;
import com.bfd.parse.entity.BaseEntity;
import com.bfd.parse.entity.PagedefineEntity;

public class PageDefineConfig extends AConfig {
	private static PageDefineConfig instance;
//	private ReadWriteLock rwLockPD = new ReentrantReadWriteLock(true);
	private static final Log logger = LogFactory.getLog(AConfig.class);
	
	private PageDefineConfig(){
		
	}
	public static PageDefineConfig getInstance() {
		if (instance == null) {
			synchronized (PageDefineConfig.class) {
				if (instance == null) {
					instance = new PageDefineConfig();
				}
			}
		}
		return instance;
	}
	
	@Override
	public boolean  requestConfig() {
		boolean rs = super.requestConfig();
		rwLockPD.writeLock().lock();
		try {
			Iterator<Entry<String, BaseEntity>> it = this.getData().entrySet().iterator();
			while(it.hasNext()){
				Entry<String, BaseEntity> entry = it.next();
				PagedefineEntity pd = (PagedefineEntity)entry.getValue();
				this.pagedefinenameIdMap.put(pd.getPagenameen(), pd.getPagetypeid());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			rwLockPD.writeLock().unlock();
		}
		logger.info("nameIdMap:"+JsonUtils.toJSONString(pagedefinenameIdMap));
		return rs;
	}
	public PagedefineEntity getPageDefine(String cacheKey) {
		Object o = this.searchByCacheKey(cacheKey);
		return (PagedefineEntity)o;
	}
	
	public String getIdByName(String name){
		if(name==null){
			return null;
		}
//		return this.nameIdMap.get(name).toString();
		
		String rs = "";
		try {
			rwLockPD.readLock().lock();
			rs = this.pagedefinenameIdMap.get(name.trim()).toString();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			rwLockPD.readLock().unlock();
		}
//		logger.info("getIdByName nameIdMap:"+JsonUtils.toJSONString(pagedefinenameIdMap)+",param:"+name+",rs:"+rs);
		return rs;
	}
	
	@Override
	public String getTableName() {
		
		return "PageDefine";
	}

	@Override
	public BaseEntity getObjByMap(Map map) {
		
		return JacksonUtils.extractObject(JsonUtils.toJSONString(map), PagedefineEntity.class);
	}

}
