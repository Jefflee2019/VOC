package com.bfd.parse.config.shelf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.EncodeUtil;
import com.bfd.crawler.utils.FileUtil;
import com.bfd.parse.client.ConfigClient;
import com.bfd.parse.config.Config;
import com.bfd.parse.util.JsonUtil;

/**
 * 上下架规则
 * @author wenchao.fu
 *
 */
public class JudgeRuleConfig implements Config {

	private static final Log log = LogFactory.getLog(JudgeRuleConfig.class);

	private static final String CONFIG_NAME = "judge_rule_config";

	private static JudgeRuleConfig instance;

	private Map<String, List<JudgeRule>> rules = new ConcurrentHashMap<String, List<JudgeRule>>();

	private ConfigClient configService = new ConfigClient();

	protected JudgeRuleConfig() {
		requestConfig();
	}

	public static JudgeRuleConfig getInstance() {
		if (instance == null) {
			synchronized (JudgeRuleConfig.class) {
				if (instance == null) {
					instance = new JudgeRuleConfig();
				}
			}
		}
		return instance;
	}

	public static List<JudgeRule> parseRules(String rules) {
		List<JudgeRule> rList = new ArrayList<JudgeRule>();
		try {
			Object o = JsonUtil.parseObject(rules);
			Map<String, Object> result = (Map<String, Object>) o;
			if (result.containsKey("code") && result.containsKey("data")) {
				if ((Integer) result.get("code") == 0) {
					List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("data");
					for (Map<String, Object> tmpl : list) {
						JudgeRule template = JudgeRule.create(tmpl);
						rList.add(template);
					}
					return rList;
				}
			}
		} catch (Exception e) {
			log.warn("parse json exception", e);
		}
		return rList;
	}

	@Override
	public boolean requestConfig() {
		String jsonRules = configService.getConfig("ALL", JsonUtil.REQUEST_JUDGE_RULES);
//		log.info("jsonRules:"+jsonRules);
		if (jsonRules == null) {
			return false;
		}
		List<JudgeRule> ruleList = parseRules(jsonRules);
		if (ruleList.size() == 0)
			return false;
//		rules = null;
//		rules = new ConcurrentHashMap<String, List<JudgeRule>>();
		rules.clear();
		for (JudgeRule rule : ruleList) {
			String bid = rule.getBid();
//			System.out.println("bid==null?"+(bid==null));
			if(bid==null){
				continue;
			}
			List<JudgeRule> list = null;
			if (rules.containsKey(bid)) {
				list = rules.get(bid);
			} else {
				list = new ArrayList<JudgeRule>();
			}
			list.add(rule);
			rules.put(bid, list);
		}
		return true;
	}

	public List<JudgeRule> getJudgeRules(String bid) {
		if (rules != null && rules.containsKey(bid)) {
			return rules.get(bid);
		}
		return null;
	}

	public List<JudgeRule> getJudgeRules(String bid, String type) {
		if (rules != null && rules.containsKey(bid)) {
			List<JudgeRule> rulelist = rules.get(bid);
			List<JudgeRule> results = new ArrayList<JudgeRule>();
			if ("list".equals(type)) {
				for (JudgeRule rule : rulelist) {
					if ("list".equals(rule.getPageType())) {
						results.add(rule);
					}
				}
			} else {
				for (JudgeRule rule : rulelist) {
					if (!"list".equals(rule.getPageType())) {
						results.add(rule);
					}
				}
			}
			return results;
		}
		return null;
	}

	public Map<String, List<JudgeRule>> getJudgeRules() {
		return rules;
	}

	@Override
	public String name() {
		return CONFIG_NAME;
	}

	public static void main(String[] args) {
		byte[] data = FileUtil.readFromFile("/home/ian/work/bizdata/domparser/src.html");
		String encode = EncodeUtil.getHtmlEncode(data);
		JudgeRuleConfig judgeRuleConfig = JudgeRuleConfig.getInstance();
		List<JudgeRule> rules = judgeRuleConfig.getJudgeRules("taoShop");
		String itemStatus = null;
		if (rules != null) {
			try {
				itemStatus = new JudgeStatue().judgeStatus("http://item.taobao.com/item.htm?id=12929684164",
						new String(data, encode), rules);
				log.info("OnShelf RES: " + itemStatus + ", biz=" + "taoShop");
			} catch (Exception x) {
			}
		}
	}

	@Override
	public Map getData() {
		return null;
	}
}
