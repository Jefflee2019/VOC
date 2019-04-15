package com.bfd.parse.config.website;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.config.Config;
//import com.bfd.parse.data.FingerprintMap;
//import com.bfd.parse.data.NormalizeRuleMap;
//import com.bfd.parse.data.ParseRuleMap;
//import com.bfd.parse.data.TitleRuleMap;
//import com.bfd.parse.data.WebsiteMap;

public class AutoRuleWrap implements Config {
	private static final Log LOG = LogFactory.getLog(AutoRuleWrap.class);

	private static final String CONFIG_NAME = "autorule";

	private static AutoRuleWrap instance;

	protected AutoRuleWrap() {
		requestConfig();
	}

	public static AutoRuleWrap getInstance() {
		if (instance == null) {
			synchronized (AutoRuleWrap.class) {
				if (instance == null) {
					instance = new AutoRuleWrap();
				}
			}
		}
		return instance;
	}

	@Override
	public boolean requestConfig() {
//		LOG.info("reloading auto parse rule");
//		FingerprintMap.INSTANCE.load();
//		ParseRuleMap.load();
//		TitleRuleMap.INSTANCE.load();
//		WebsiteMap.load();
//		NormalizeRuleMap.INSTANCE.load();
		return true;
	}

	@Override
	public String name() {
		return CONFIG_NAME;
	}

	public static void main(String[] args) {
		getInstance().requestConfig();
	}

	@Override
	public Map getData() {
		return null;
	}
}
