package com.bfd.parse.json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.TextUtil;

/**
 * 驱动之家新闻
 * 内容页面的动态数据
 * 取浏览数
 * @author bfd_05
 *
 */
public class NmydriversContentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NmydriversContentJson.class);
	public void executeParse(Map<String, Object> parseData, String json,
			String url, ParseUnit unit) {
		String[] viewCnts = json.split(";");
		parseData.put(Constants.VIEW_COUNT, viewCnts[0].replace("var hits =", "").trim());
	}

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList,
			URLNormalizerClient paramURLNormalizerClient,
			ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parseData = new HashMap<String, Object>();
		// 遍历dataList
		for (JsonData jsonData : dataList) {
			if (!jsonData.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(jsonData, unit);
			executeParse(parseData, json, jsonData.getUrl(), unit);
		}
		// 组装返回结果
		JsonParserResult result = new JsonParserResult();
		try {
			result.setParsecode(parsecode);
			result.setData(parseData);
		} catch (Exception e) {
//			e.printStackTrace();
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

}
