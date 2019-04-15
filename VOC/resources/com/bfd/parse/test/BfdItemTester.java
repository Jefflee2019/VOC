package com.bfd.parse.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.config.fldmap.BfdItemFldMapRule;
import com.bfd.parse.config.fldmap.ItemInfoParser2;

public class BfdItemTester implements Tester {

	private static final Log LOG = LogFactory.getLog(BfdItemTester.class);

	private static final Map<String, String> dftRule = new HashMap<String, String>();

	private static final List<String> requiredFlds = new ArrayList<String>();
	static {
		dftRule.put("id", "iid");
		dftRule.put("name", "name");
		dftRule.put("image_link", "large_img");
		dftRule.put("price", "price");
		dftRule.put("market_price", "market_price");
		dftRule.put("category", "cate");
		dftRule.put("city", "location");
		dftRule.put("region", "district");
		dftRule.put("del_time", "del_time");
		dftRule.put("sales_num", "sales_num");
		dftRule.put("discount", "discount");
		dftRule.put("longitude", "longitude");
		dftRule.put("latitude", "latitude");
		dftRule.put("category_name", "cate");
		dftRule.put("tag", "tag");
		// dftRule.put("onsale", "onshelves");

		requiredFlds.add("id");
		requiredFlds.add("name");
		requiredFlds.add("price");
		requiredFlds.add("image_link");
		// dftRequiredFldSet.add("category");
	}

	private URLNormalizerClient normalizer;

	public BfdItemTester(URLNormalizerClient normalizer) {
		this.normalizer = normalizer;
	}

	@Override
	public TestResponse test(TestRequest req) {
		Map<String, Object> reqMap = req.getReqMap();
		String action = (String) reqMap.get("action");
		if ("preAdd".equalsIgnoreCase(action)) {
			return preAdd(req, reqMap);
		} else {
			return test(req, reqMap);
		}
	}

	private TestResponse preAdd(TestRequest req, Map<String, Object> reqMap) {
		String bfdItemJs = ItemInfoParser2.getBfdItemJs(req.getData());
		if (StringUtils.isNotEmpty(bfdItemJs)) {
			Map<String, Object> itemInfo = ItemInfoParser2.getItemInfoFromBfdJs(bfdItemJs);
			Map<String, Object> result = new HashMap<String, Object>();
			if (itemInfo != null && itemInfo.size() > 0) {
				Map<String, String> directFields = new HashMap<String, String>();
				for (Entry<String, Object> entry : itemInfo.entrySet()) {
					String key = entry.getKey().replace("\"", "").replace("'", "");
					result.put(key, entry.getValue());
					String value = dftRule.get(key);
					if (StringUtils.isNotEmpty(value)) {
						directFields.put(key, value);
					}
				}
				BfdItemFldMapRule rule = new BfdItemFldMapRule(null, null, directFields, requiredFlds, null, null,
						null, null, "0");
				return TestResponse.create(0).put("bfdItemJs", bfdItemJs).put("itemInfo", result).put("rule", rule);
			} else {
				return TestResponse.create(1).put("msg", "解析JS代码，没有获得结果！");
			}
		}
		return TestResponse.create(1).put("msg", "没有获取到实施JS代码！");
	}

	private TestResponse test(TestRequest req, Map<String, Object> reqMap) {
		if (!checkTestRequest(req)) {
			return TestResponse.create(1);
		}
		Map<String, Object> result = new HashMap<String, Object>();
		BfdItemFldMapRule rule = null;
		StringBuilder errMsg = new StringBuilder();
		try {
			rule = BfdItemFldMapRule.fromMap((Map) reqMap.get("rule"));
		} catch (Exception e) {
			LOG.warn(e);
		}
		StringBuilder bfdJs = new StringBuilder();
		boolean success = ItemInfoParser2.getBfdItemInfo(req.getData(), req.getCid(), req.getUrl(), 
				normalizer, rule,result, bfdJs, errMsg);
		if (success) {
			return TestResponse.create(0).put("result", result).put("bfdItemJs", bfdJs.toString());
		}
		return TestResponse.create(1).put("errMsg", errMsg.toString());
	}

	private boolean checkTestRequest(TestRequest request) {
		if (StringUtils.isEmpty(request.getUrl()) || StringUtils.isEmpty(request.getPageData())) {
			return false;
		}
		return true;
	}
}
