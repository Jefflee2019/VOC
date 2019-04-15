package com.bfd.parse.json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * 站点名：Nimobile
 * 
 * 功能：动态获取浏览数
 * 
 * @author bfd_04
 */
public class NimobileContentJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(NimobileContentJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient urlnormalizerClients,
			ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();

		for (Object obj : dataList) {
			JsonData jsonData = (JsonData) obj;
			if (!jsonData.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(jsonData, unit);
			try {
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
						&& (json.indexOf("[")) < (json.indexOf("]"))) {
					json = json.substring(json.indexOf("["),
							json.indexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") >= 0
						&& json.indexOf("{") < json.indexOf("}")) {
					json = json.substring(json.indexOf("{"),
							json.indexOf("}") + 1);
				}
				executeParse(parsedata, json, jsonData.getUrl(), unit);
			} catch (Exception e) {
//				e.printStackTrace();
				LOG.warn("JsonParser exception, taskdata url="
								+ taskdata.get("url") + ".jsonUrl :"
								+ jsonData.getUrl(), e);
			}
		}

		JsonParserResult result = new JsonParserResult();
		try {
			result.setParsecode(parsecode);
			result.setData(parsedata);
		} catch (Exception e) {
//			e.printStackTrace();
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		Object obj = null;
		try {
				obj = JsonUtil.parseObject(json);
			} catch (Exception e) {
//				e.printStackTrace();
				LOG.error("jsonparser reprocess error url:" + url);
			}
			if(obj instanceof Map){
				Map<String, Object> jsonMap = (Map<String, Object>) obj; 
				if (jsonMap.containsKey("pv")) {
					parsedata.put(Constants.VIEW_CNT, jsonMap.get("pv"));
				}
			}
	}
}
