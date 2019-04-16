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
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * 站点名：优酷
 * 
 * 功能：动态获取 评论数 播放总数
 * 
 * @author bfd_06
 * 
 */
public class NyoukuContentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NyoukuContentJson.class);
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
						&& (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["),
							json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"),
							json.lastIndexOf("}") + 1);
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
			LOG.error("JsonParse reprocess error, taskdat url=" + taskdata.get("url"), e);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		/**
		 * JSON插件需要手加入tasks 防止返回错误
		 */
		List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
		parsedata.put("tasks", tasks);
		try {
			Map<String, String> result = (Map<String, String>) JsonUtil
					.parseObject(json);
			/**
			 * 获取评论总数
			 */
			if (url.contains("comments")) {
				if (result.containsKey("total")) {
					parsedata.put(Constants.REPLY_CNT, result.get("total"));
				}
				/**
				 * 获取播放次数
				 */
			} else {
				if (result.containsKey("vv")) {
					parsedata.put(Constants.PLAY_CNT, result.get("vv"));
				}
			}
		} catch (Exception e) {
			LOG.error(
					"json format conversion error in the executeParse() method",
					e);
		}

	}

	public String match(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return matcher.group(1);
		}

		return null;
	}
	
}
