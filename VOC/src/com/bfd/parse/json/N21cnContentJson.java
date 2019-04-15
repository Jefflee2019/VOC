package com.bfd.parse.json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.TextUtil;
/**
 * 站点名：21cn
 * <p>
 * 主要功能：取得新闻送鲜花的数量和扔鸡蛋的数量
 * @author bfd_01
 *
 */
public class N21cnContentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(N21cnContentJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient normalizerClient,
			ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
		for (JsonData data : dataList) {
			if (!data.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(data, unit);
			LOG.info("url:" + data.getUrl() + ".json is " + json);
			try {
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
						&& (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["),
							json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"),
							json.lastIndexOf("}") + 1);
				}
				LOG.info("url:" + data.getUrl() + ".correct json is " + json);

				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
				LOG.warn("json :" + json + ".url:" + taskdata.get("url"));
				parsecode = 500012;
				LOG.warn(
						"AMJsonParser exception, taskdata url="
								+ taskdata.get("url") + ".jsonUrl :"
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
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		try {
			Map<String, Object> map = (Map<String, Object>) JsonUtils
					.parseObject(json);
			if (map.containsKey(Constants.NEW_EGGNUM)) {
				int eggnum = Integer.parseInt(map.get(Constants.NEW_EGGNUM)
						.toString());
				parsedata.put(Constants.NEW_EGGNUM, eggnum);
			}
			if (map.containsKey(Constants.NEW_FLOWERNUM)) {
				int flowernum = Integer.parseInt(map.get(
						Constants.NEW_FLOWERNUM).toString());
				parsedata.put(Constants.NEW_FLOWERNUM, flowernum);
			}
		} catch (Exception e) {
			LOG.error(e);
		}
	}
}
