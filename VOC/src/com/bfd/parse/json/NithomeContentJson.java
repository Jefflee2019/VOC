package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.TextUtil;

/**
 * 站点名：IT之家
 * 
 * 功能：动态获取浏览数、评论数
 * 
 * @author bfd_06
 */
public class NithomeContentJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(NithomeContentJson.class);
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient urlnormalizerClients,
			ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();

		for (Object obj : dataList) {
			JsonData data = (JsonData) obj;
			if (!data.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(data, unit);
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
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
				LOG.error("JsonParse reprocess exception, taskdat url="
						+ taskdata.get("url") + ".jsonUrl:" + data.getUrl(), e);
			}
		}

		JsonParserResult result = new JsonParserResult();
		try {
			result.setParsecode(parsecode);
			result.setData(parsedata);
		} catch (Exception e) {
			LOG.error("JsonParse reprocess error url:" + taskdata.get("url"), e);
		}
		return result;
	}

	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		/**
		 * 加上tasks
		 */
		List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
		parsedata.put("tasks", tasks);
		if (url.contains("HitCount")) {
			/**
			 * 获取浏览总数
			 */
			String value = match("<strong>(\\d+)</strong>", json);
			parsedata.put(Constants.VIEW_CNT, value);
		} else {
			/**
			 * 获取评论总数
			 */
			String value = match("innerHTML = '(\\d+)'", json);
			parsedata.put(Constants.REPLY_CNT, value);
		}
	}

	public String match(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return matcher.group(1);
		}
		
		return "0";
	}
	
}
