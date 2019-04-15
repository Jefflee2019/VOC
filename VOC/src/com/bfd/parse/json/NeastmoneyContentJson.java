package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * 站点名：Neastmoney
 * 
 * 获取评论总数,参与人总数
 * 
 * @author bfd_06
 * 
 */
public class NeastmoneyContentJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(NeastmoneyContentJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient urlnormalizerClients,
			ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
		/**
		 * JsonData为List的原因为jsEngine有时会请求好几个链接
		 */
		for (Object obj : dataList) {
			JsonData data = (JsonData) obj;
			if (!data.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(data, unit);
			int indexA = json.indexOf("(");
			int indexB = json.lastIndexOf(")");
			if (indexA >= 0 && indexB >= 0 && indexA < indexB) {
				json = json.substring(indexA + 1, indexB);
			}
			executeParse(parsedata, json, data.getUrl(), unit);
		}
		JsonParserResult result = new JsonParserResult();
		result.setParsecode(parsecode);
		result.setData(parsedata);
		return result;
	}

	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		/**
		 * 加上tasks
		 */
		List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
		parsedata.put("tasks", tasks);
		try {
			Map<String, Object> result = (Map<String, Object>) JsonUtil
					.parseObject(json);
			List<Map<String, Object>> re = (List<Map<String, Object>>) result.get("re");
			Map<String, Object> firstMap = re.get(0);
			// JREPLY_CNT
			parsedata.put("jreply_cnt", firstMap.get("post_click_count"));
			// REPLY_CNT
			parsedata.put("reply_cnt", firstMap.get("post_comment_count"));
			// POST_ID 评论页标志id
			parsedata.put("post_id", firstMap.get("post_id"));
		} catch (Exception e) {
			LOG.error(
					"json format conversion error in the executeParse() method",
					e);
		}
		
//		System.out.println(parsedata.toString());

	}

}
