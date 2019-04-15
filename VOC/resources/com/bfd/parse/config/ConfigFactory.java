package com.bfd.parse.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.config.PageDefine.PageDefineConfig;
import com.bfd.parse.config.dom.DomConfig;
import com.bfd.parse.config.iid.ParseConfigure;
import com.bfd.parse.config.parseplugin.PluginConfig;
import com.bfd.parse.config.shelf.JudgeRuleConfig;
import com.bfd.parse.config.sitepageconfig.SitePageConfigCache;
import com.bfd.parse.config.website.WebsiteCache;

public class ConfigFactory {
	private static final Log log = LogFactory.getLog(ConfigFactory.class);
	public static Config getInstance(String tableName){
		Config config = null ; 
		if("parsetemplate".equals(tableName)){
			config = DomConfig.getInstance();
		}else if("parseplugin".equals(tableName)){
			config = PluginConfig.getInstance();
		}else if("sitepageconfig".equals(tableName)){
			config = SitePageConfigCache.getInstance();
		}else if("website".equals(tableName)){
			config = WebsiteCache.getInstance();
			
		}else if("pagedefine".equals(tableName)){
			config = PageDefineConfig.getInstance();
		}
		else{
			log.error("tablename:"+tableName+" no get config to reload ");
			return null;
		}
		log.debug("tableName : "+tableName + " return "+config.getClass().getName());
		return config;
	}
}
