package com.bfd.parse.zkmonitor;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.config.util.CrawlMonitor.ICrawlMonitor;
import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.Constants;
import com.bfd.parse.config.ConfigFactory;

public class ParseMonitor implements ICrawlMonitor {
	public static ParseMonitor monitor = new ParseMonitor();
	private static final Log LOG = LogFactory.getLog(ParseMonitor.class);
	//arg0为监听节点发生变化的时候，返回的数据
	@Override
	public void changes(String arg0) {
		Map<String, Object> monitorMap = null;
		try {
			monitorMap = JsonUtils.parseObject(arg0);
		} catch (Exception e) {
			LOG.error("parse monitor rs error!");
			e.printStackTrace();
			return ;
		}
		if(!monitorMap.containsKey(Constants.tablename)){
			LOG.error("monitroMap no contain tablename!");
			return;
		}
		String tableName = monitorMap.get(Constants.tablename).toString();
		LOG.info("begin reload "+tableName);
		ConfigFactory.getInstance(tableName).requestConfig();
	}

}
