package com.bfd.parse.json;

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
 * 站点名：天涯博文
 * 
 * 功能：获取今日浏览数 总浏览数
 * 
 * @author bfd_06
 */
public class NtianyaContentJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(NtianyaContentJson.class);

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
			LOG.error("JsonParse reprocess error, taskdat url=" + taskdata.get("url"), e);
		}
		return result;
	}

	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		// 今日访问数
		String value = match("Count\":(\\d+)", json);
		parsedata.put(Constants.TODAY_VISIT_CNT, value);
		// 浏览数
		String value2 = match("Click\":(\\d+)", json);
		parsedata.put(Constants.VIEW_CNT, value2);
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
