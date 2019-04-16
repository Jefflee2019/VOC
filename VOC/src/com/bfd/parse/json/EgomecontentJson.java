package com.bfd.parse.json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.TextUtil;

public class EgomecontentJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(EgomecontentJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient normalizerClient, ParseUnit unit) {

		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();

		for (JsonData data : dataList) {
			if (!data.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(data, unit);
			LOG.info("url:" + data.getUrl() + ".json is " + json);
			try {
				LOG.info("url:" + data.getUrl() + ".correct json is " + json);
				if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
				}
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
				LOG.warn("json :" + json + ".url:" + taskdata.get("url"));
				parsecode = 500012;
				LOG.warn(
						"EGomecontentJsonParser exception, taskdata url=" + taskdata.get("url") + ".jsonUrl :"
								+ data.getUrl(), e);
			}
		}
		JsonParserResult result = new JsonParserResult();
		try {
			result.setData(parsedata);
			result.setParsecode(parsecode);
		} catch (Exception e) {
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;

	}

	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata, String json, String url, ParseUnit unit) {
		// {"price":"139.00","proms":[{"site_enable":"0","type":"LYMANFAN","iconText":"返券","desc":"满500元，送1张10元店铺券；满1,000元，送1张20元店铺券；满2,000元，送1张30元店铺券","titleList":[]}]}
		try {
			Map<String, Object> map = (HashMap<String, Object>) JsonUtils.parseObject(json);
			String price = (String) map.get("price");
			parsedata.put("price", price);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
