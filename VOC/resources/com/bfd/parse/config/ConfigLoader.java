package com.bfd.parse.config;

import java.util.HashMap;
import java.util.Map;

import com.bfd.parse.config.dom.DomConfig;
import com.bfd.parse.config.parseplugin.PluginConfig;
import com.bfd.parse.config.shelf.JudgeRuleConfig;
import com.bfd.parse.config.website.AutoRuleWrap;

public class ConfigLoader {

	private static Map<String, Config> configMrgDict;
	static {
		configMrgDict = new HashMap<String, Config>();
		configMrgDict.put("tmpl", DomConfig.getInstance());
		configMrgDict.put("plugin", PluginConfig.getInstance());
		configMrgDict.put("rule", JudgeRuleConfig.getInstance());
		configMrgDict.put("autorule", AutoRuleWrap.getInstance());
	}

	public static boolean load(Map<String, Object> reqMap) {
		String configType = (String) reqMap.get("config");// tmpl,plugin,rule
		Config config = configMrgDict.get(configType);
		if (config instanceof DomConfig && !"all".equals((String) reqMap.get("cid"))) {
			String cid = (String) reqMap.get("cid"); // all , Czouxiu, doit
			String type = null;
			if (reqMap.containsKey("type")) {
				type = (String) reqMap.get("type"); // list, info, item
			}
			DomConfig domConfig = (DomConfig) config;
			return domConfig.requestConfig(cid, type);
		} else {
			return config.requestConfig();
		}
	}
}
