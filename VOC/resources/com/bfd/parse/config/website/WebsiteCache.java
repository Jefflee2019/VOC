package com.bfd.parse.config.website;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.JacksonUtils;
import com.bfd.parse.client.ConfigClient;
import com.bfd.parse.config.AConfig;
import com.bfd.parse.config.Config;
import com.bfd.parse.entity.BaseEntity;
import com.bfd.parse.entity.WebsiteEntity;
import com.bfd.parse.util.JsonUtil;

public class WebsiteCache extends AConfig {
	private static final Log logger = LogFactory.getLog(WebsiteCache.class);
	private static final String CONFIG_NAME = "website";

//	private Map<String, Website> siteCache = new ConcurrentHashMap<String, Website>();
//	private ConfigClient service = new ConfigClient();
//	private ReadWriteLock rwLock = new ReentrantReadWriteLock();

	private static WebsiteCache instance;

	private WebsiteCache() {
		
	}

	public static WebsiteCache getInstance() {
		if (instance == null) {
			synchronized (WebsiteCache.class) {
				if (instance == null) {
					instance = new WebsiteCache();
				}
			}
		}
		return instance;
	}

//	@Override
//	public boolean requestConfig() {
//		try {
//			String json = service.getConfig("all", JsonUtil.REQUEST_WEBSITE);
////			logger.info("load website data json is "+json);
//			Map<String, Object> result = (Map<String, Object>) JsonUtil.parseObject(json);
//			if ((Integer) result.get("code") == 0) { // TODO
//				Map<String, Website> siteMap = new ConcurrentHashMap<String, Website>();
//				List<Map<String, Object>> dataList = (List<Map<String, Object>>) result.get("data");
//				if (dataList != null) {
//					for (Map<String, Object> data : dataList) {
//						Website site = Website.fromMap(data);
//						if (site != null) {
//							siteMap.put(site.getCid(), site);
//						}
//					}
//				}
//				if (!siteMap.isEmpty()) {
//					logger.info("Request WebsiteConfigs successfully, size: " + siteMap.size());
////					rwLock.writeLock().lock();
//					siteCache = siteMap;
////					rwLock.writeLock().unlock();
//					return true;
//				}
//			}
//		} catch (Exception e) {
//			logger.warn("Request WebsiteConfigs exception.", e);
//		}
//		return false;
//	}

	public WebsiteEntity getWebsite(String cacheKey) {
		Object o = this.searchByCacheKey(cacheKey);
		return (WebsiteEntity)o;
	}

//	public Map<String, Website> getWebsites() {
//		return siteCache;
//	}

	@Override
	public String name() {
		return CONFIG_NAME;
	}

	@Override
	public String getTableName() {
		return "website";
	}

	@Override
	public BaseEntity getObjByMap(Map map) {
		return JacksonUtils.extractObject(JacksonUtils.compressObject(map), WebsiteEntity.class);
	}

	

//	@Override
//	public Map getData() {
//		return siteCache;
//	}

}
