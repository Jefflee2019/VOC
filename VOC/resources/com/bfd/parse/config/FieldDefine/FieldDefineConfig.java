package com.bfd.parse.config.FieldDefine;

import java.util.Map;

import com.bfd.crawler.utils.JacksonUtils;
import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.config.AConfig;
import com.bfd.parse.config.PageDefine.PageDefineConfig;
import com.bfd.parse.entity.BaseEntity;
import com.bfd.parse.entity.FielddefineEntity;

public class FieldDefineConfig extends AConfig {
	private static FieldDefineConfig instance;
	private FieldDefineConfig(){
		
	}
	public FielddefineEntity getFieldDefine(String cacheKey){
		return (FielddefineEntity)this.searchByCacheKey(cacheKey);
	}
	public static FieldDefineConfig getInstance() {
		if (instance == null) {
			synchronized (FieldDefineConfig.class) {
				if (instance == null) {
					instance = new FieldDefineConfig();
				}
			}
		}
		return instance;
	}
	@Override
	public String getTableName() {
		return "FieldDefine";
	}

	@Override
	public BaseEntity getObjByMap(Map map) {
		return JacksonUtils.extractObject(JsonUtils.toJSONString(map), FielddefineEntity.class);
	}

}
