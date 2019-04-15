package com.bfd.parse.config.iid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.ConfigClient;
import com.bfd.parse.config.Config;
import com.bfd.parse.util.JsonUtil;
//iid规则加载,b版没有这个表了，可以去掉了
public class ParseConfigure implements Config {

	private static final Log log = LogFactory.getLog(ParseConfigure.class);

	private static final String CONFIG_NAME = "parseConfig";

	private static ParseConfigure instance;

	private Map<String, ParseCfg> rules = new ConcurrentHashMap<String, ParseCfg>();

	private ConfigClient configService = new ConfigClient();

	protected ParseConfigure() {
		requestConfig();
	}

	public static ParseConfigure getInstance() {
		if (instance == null) {
			synchronized (ParseConfigure.class) {
				if (instance == null) {
					instance = new ParseConfigure();
				}
			}
		}
		return instance;
	}

	//TODO:iid规则从sitePageConfig表得到,这个类不需要了
	@Override
	public boolean requestConfig() {
		String jsonRules = configService.getConfig("ALL", JsonUtil.REQUEST_PARSECONFIG);
		log.trace("iid ");
		if (StringUtils.isNotEmpty(jsonRules)) {
			try {
				List<ParseCfg> ruleList = new ArrayList<ParseCfg>();
				Map<String, Object> result = (Map<String, Object>) JsonUtil.parseObject(jsonRules);
				if ((Integer) result.get("code") == 0) {
					List<Map<String, Object>> dataList = (List<Map<String, Object>>) result.get("data");
					for (Map<String, Object> data : dataList) {
						ParseCfg rule = ParseCfg.fromMap(data);
						ruleList.add(rule);
					}
				}
				if (ruleList.size() == 0)
					return false;
				synchronized (result) {
					rules.clear();
					log.info("Request item field mapRule success, ruleList size is: " + ruleList.size());
					for (ParseCfg rule : ruleList) {
						rules.put(rule.getCid(), rule);
					}
				}
				return true;
			} catch (Exception e) {
				log.warn("exception", e);
			}
		}
		return false;
	}

	public ParseCfg getParseConfig(String cid) {
//		log.info("execute getParseConfig cid:"+cid);
		synchronized (this) {
			if (rules != null && rules.containsKey(cid)) {
				return rules.get(cid);
			}
		}
		return null;
	}

	public Map<String, ParseCfg> getParseConfigs() {
		return rules;
	}

	@Override
	public String name() {
		return CONFIG_NAME;
	}

	public static void main(String[] args) {
		ParseConfigure config = ParseConfigure.getInstance();
		config.requestConfig();
		ParseCfg rule = config.getParseConfig("Cdiyicaijing");
	}

	@Override
	public Map getData() {
		return this.rules;
	}
}
