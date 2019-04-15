package com.bfd.parse.config.shelf;

import java.util.Map;

import com.bfd.crawler.utils.JacksonUtils;
import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.config.AConfig;
import com.bfd.parse.config.sitepageconfig.SitePageConfigCache;
import com.bfd.parse.entity.BaseEntity;
import com.bfd.parse.entity.ECConfigEntity;
import com.bfd.parse.entity.SitepageconfigEntity;

public class EcConfigCache extends AConfig {
	public static EcConfigCache instance;
	public static EcConfigCache getInstance() {
		if (instance == null) {
			synchronized (SitePageConfigCache.class) {
				if (instance == null) {
					instance = new EcConfigCache();
				}
			}
		}
		return instance;
	}
	
	private EcConfigCache() {
		super();
	}

	public ECConfigEntity getECConfig(String cacheKey) {
		Object rs = this.searchByCacheKey(cacheKey);
		if(rs == null){
			return null;
		}
		return (ECConfigEntity)rs;
	}

	@Override
	public String name() {
		return this.getClass().getName();
	}

	@Override
	public Map getData() {
		
		return this.cache;
	}
	@Override
	public String getTableName() {
		return "ecConfig";
	}

	@Override
	public BaseEntity getObjByMap(Map map) {
		ECConfigEntity entity = JacksonUtils.extractObject(JacksonUtils.compressObject(map), ECConfigEntity.class);
		return entity;
	}

	public static void main(String[] args) {
		EcConfigCache cache = new EcConfigCache();
		System.out.println(JsonUtils.toJSONString(cache.getData()));
	}
}
