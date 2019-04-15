package com.bfd.parse.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.ParserFace;
import com.bfd.parse.client.ConfigClient;
import com.bfd.parse.entity.BaseEntity;
import com.bfd.parse.util.JsonUtil;

public abstract class AConfig implements Config {
	private static final Log logger = LogFactory.getLog(AConfig.class);
	protected ConfigClient configService = new ConfigClient();
	protected Map<String, BaseEntity> cache = new HashMap<String, BaseEntity>();
	private ReadWriteLock rwLock = new ReentrantReadWriteLock(true);
	protected ReadWriteLock rwLockPD = new ReentrantReadWriteLock(true);
	protected Map<String, Object> pagedefinenameIdMap = new HashMap<String, Object>();
	public abstract String getTableName();
	public abstract BaseEntity getObjByMap(Map map);
	
	
	public AConfig() {
		super();
		requestConfig();
	}
	@Override
	public boolean requestConfig() {
		logger.info("execute requestConfig tablename is "+getTableName());
		rwLock.writeLock().lock();
		String config = configService.getConfig(getTableName(), REQUEST_ALL);
		logger.info("requestconfig "+getTableName()+" config is :"+config);
		if (config == null) {
			rwLock.writeLock().unlock();
			return false;
		}
		this.cache.clear();
		Map map;
		try {
			map = (Map) JsonUtils.parseObject(config);
			List<Map> data = (List<Map>) map.get("data");
//			List list = new ArrayList();
			for (Map tempMap : data) {
				BaseEntity o = getObjByMap(tempMap);
				//TODO这个有效性判断，在getObjByMap实现，如果失效就返回null
				if (o!=null) {
					//针对一个cachekey有多个记录的情况，这是结果是一个链表。
					//比如模板：一个siteId 一个pageTypeId可能又多个模板
					BaseEntity firstObj = this.cache.get(o.getCacheKey());
					if(firstObj!=null){
//						getLastEntity(this.cache.get(o.getCacheKey())).setNext(o);
						//domtemplate 通过template来增加模板
						firstObj.addEntityToLast(o);
					}else{
						
						cache.put(o.getCacheKey(), o);
						
					}
						
				}
			}
			logger.debug("table:"+getTableName()+",cache size is "
			+this.cache.size()+".cache is "+JsonUtils.toJSONString(this.cache));
		} catch (Exception e) {
			e.printStackTrace();
//			rwLock.writeLock().unlock();
			return false;
		}finally{
			rwLock.writeLock().unlock();
		}
		
		return true;
	}
	
	public Object searchByCacheKey(String cacheKey){
		Object rs = null;
		try {
			rwLock.readLock().lock();
			rs = this.cache.get(cacheKey);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			rwLock.readLock().unlock();
		}
		if(rs==null){
			logger.info("searchbycachekey return null cache is "+JsonUtils.toJSONString(this.cache));
		}
		return rs;
		
	}
	

	@Override
	public String name() {
		return null;
	}

	@Override
	public Map<String, BaseEntity> getData() {
		return this.cache;
	}

}
