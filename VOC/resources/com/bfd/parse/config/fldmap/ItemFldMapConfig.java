package com.bfd.parse.config.fldmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.EncodeUtil;
import com.bfd.crawler.utils.FileUtil;
import com.bfd.parse.client.ConfigClient;
import com.bfd.parse.config.Config;
import com.bfd.parse.util.JsonUtil;
//百分点js规则
public class ItemFldMapConfig implements Config {

	private static final Log log = LogFactory.getLog(ItemFldMapConfig.class);

	private static final String CONFIG_NAME = "ItemFldMapRule";

	private static ItemFldMapConfig instance;

	private Map<String, BfdItemFldMapRule> rules = new ConcurrentHashMap<String, BfdItemFldMapRule>();

	private ConfigClient configService = new ConfigClient();

	protected ItemFldMapConfig() {
		requestConfig();
	}

	public static ItemFldMapConfig getInstance() {
		if (instance == null) {
			synchronized (ItemFldMapConfig.class) {
				if (instance == null) {
					instance = new ItemFldMapConfig();
				}
			}
		}
		return instance;
	}

	@Override
	public boolean requestConfig() {
		String jsonRules = configService.getConfig("ALL", JsonUtil.REQUEST_ITEMFLDMAPRULES);
		if (StringUtils.isNotEmpty(jsonRules)) {
			try {
				List<BfdItemFldMapRule> ruleList = new ArrayList<BfdItemFldMapRule>();
				Map<String, Object> result = (Map<String, Object>) JsonUtil.parseObject(jsonRules);
				if ((Integer) result.get("code") == 0) {
					List<Map<String, Object>> dataList = (List<Map<String, Object>>) result.get("data");
					for (Map<String, Object> data : dataList) {
						BfdItemFldMapRule rule = BfdItemFldMapRule.fromMap(data);
						ruleList.add(rule);
					}
				}
				if (ruleList.size() == 0)
					return false;
				synchronized (this) {
					rules.clear();
					log.info("Request item field mapRule success, ruleList size is: " + ruleList.size());
					for (BfdItemFldMapRule rule : ruleList) {
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

	public BfdItemFldMapRule getFldMapRule(String cid) {
		if (rules != null && rules.containsKey(cid)) {
			return rules.get(cid);
		}
		return null;
	}

	public Map<String, BfdItemFldMapRule> getFldMapRules() {
		return rules;
	}

	@Override
	public String name() {
		return CONFIG_NAME;
	}

	public static void main(String[] args) {
		byte[] data = FileUtil.readFromFile("/home/ian/work/bizdata/domparser/src.html");
		String encode = EncodeUtil.getHtmlEncode(data);
		ItemFldMapConfig config = ItemFldMapConfig.getInstance();
		config.requestConfig();
		BfdItemFldMapRule rule = config.getFldMapRule("C58tuan");
	}

	@Override
	public Map getData() {
		
		return this.rules;
	}
}
