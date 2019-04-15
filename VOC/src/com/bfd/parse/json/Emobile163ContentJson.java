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
 * @site：网易手机/数码
 * @function：处理商品评分等
 * @author bfd_04
 *
 */
public class Emobile163ContentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(Emobile163ContentJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient normalizerClient, ParseUnit unit) {
		int parseCode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
		for (JsonData data : dataList) {
			if (!data.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(data, unit);
			// LOG.info("url:"+data.getUrl()+".json is "+json);
			// 将ajax数据转化为json数据格式
			if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0 && (json.indexOf("[") < json.indexOf("{"))) {
				json = json.substring(json.indexOf("["), json.lastIndexOf("]") + 1);
			} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
				json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
			}
			try {
				// LOG.info("url:"+data.getUrl()+".correct json is "+json);
				@SuppressWarnings("unchecked")
				Map<String, Object> jsonMap = (Map<String, Object>) JsonUtils.parseObject(json);
				String url = data.getUrl().toString();
				if (url.contains("getOverall")) {
					getAverage(jsonMap, parsedata);
				} else if (url.contains("getCommentsCount")) {
					getReplyCnt(jsonMap, parsedata);
				}
			} catch (Exception e) {
				LOG.warn("AMJsonParser exception, taskdata url=" + taskdata.get("url") + ".jsonUrl :" + data.getUrl(),
						e);
			}

		}
		JsonParserResult result = new JsonParserResult();
		try {
			result.setData(parsedata);
			result.setParsecode(parseCode);
		} catch (Exception e) {
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

	// 获得商品评分
	private void getAverage(Map<String, Object> jsonMap, Map<String, Object> parsedata) {
		String average = "";
		if (jsonMap.containsKey("overall")) {
			average = jsonMap.get("overall").toString();
		}
		parsedata.put(Constants.AVERAGE, average);
	}

	// 获得评论总数
	private void getReplyCnt(Map<String, Object> jsonMap, Map<String, Object> parsedata) {
		String replyCnt = "";
		if (jsonMap.containsKey("count")) {
			replyCnt = jsonMap.get("count").toString();
		}
		parsedata.put(Constants.REPLY_CNT, replyCnt);
	}
}
