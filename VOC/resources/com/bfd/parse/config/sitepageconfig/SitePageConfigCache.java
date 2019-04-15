package com.bfd.parse.config.sitepageconfig;

import java.util.Map;

import com.bfd.crawler.utils.JacksonUtils;
import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.config.AConfig;
import com.bfd.parse.config.Config;
import com.bfd.parse.config.website.WebsiteCache;
import com.bfd.parse.entity.BaseEntity;
import com.bfd.parse.entity.SitepageconfigEntity;
import com.bfd.parse.entity.WebsiteEntity;

public class SitePageConfigCache extends AConfig{
	public static SitePageConfigCache instance;
	public static SitePageConfigCache getInstance() {
		if (instance == null) {
			synchronized (SitePageConfigCache.class) {
				if (instance == null) {
					instance = new SitePageConfigCache();
				}
			}
		}
		return instance;
	}
	
	private SitePageConfigCache() {
		super();
	}

	public SitepageconfigEntity getSitePageConfig(String cacheKey) {
		Object rs = this.searchByCacheKey(cacheKey);
		if(rs == null){
			return null;
		}
		return (SitepageconfigEntity)rs;
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
		return "sitePageConfig";
	}
	@Override
	public BaseEntity getObjByMap(Map map) {
		SitepageconfigEntity entity = JacksonUtils.extractObject(JacksonUtils.compressObject(map), SitepageconfigEntity.class);
		return entity;
	}

	public static void main(String[] args) {

		Config config = new SitePageConfigCache();
		System.out.println(JsonUtils.toJSONString(config.getData()));
	}

}
