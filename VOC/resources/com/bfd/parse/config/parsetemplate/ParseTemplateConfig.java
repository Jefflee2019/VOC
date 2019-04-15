package com.bfd.parse.config.parsetemplate;

import java.util.Map;

import com.bfd.crawler.utils.JacksonUtils;
import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.config.AConfig;
import com.bfd.parse.entity.BaseEntity;
import com.bfd.parse.entity.ParsetemplateEntity;
//这个无用了,用原来的domtemplate的cache吧
public class ParseTemplateConfig extends AConfig {

	@Override
	public String getTableName() {
		return "parseTemplate";
	}

	@Override
	public BaseEntity getObjByMap(Map map) {
		
		return JacksonUtils.extractObject(JacksonUtils.compressMap(map), ParsetemplateEntity.class);
	}

}
