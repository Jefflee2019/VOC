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
 * 站点名：Nyidianzixun
 * 
 * 动态解析列表页
 * 
 * @author bfd_06
 * 
 */
public class NhiapkContentJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(NhiapkContentJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient urlnormalizerClients, ParseUnit unit) {
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
			executeParse(parsedata, json, data.getUrl(), unit);
		}
		JsonParserResult result = new JsonParserResult();
		result.setParsecode(parsecode);
		result.setData(parsedata);
		return result;
	}

	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> resultData, String json, String url, ParseUnit unit) {
		try {
			Map<String, Object> result = (Map<String, Object>) JsonUtil.parseObject(json);
			Map<String, Object> data = (Map<String, Object>) result.get("data");
			Map<String, Object> detail = (Map<String, Object>) data.get("detail");
			String title = (String) detail.get("title");
			String pubtime = (String) detail.get("pubtime");
			String author = (String) detail.get("author");
			String content = (String) detail.get("content");
			
			resultData.put(Constants.POST_TIME, pubtime);
			resultData.put(Constants.AUTHOR, author);
			resultData.put(Constants.TITLE, title);
			resultData.put(Constants.CONTENT, content);
		} catch (Exception e) {
			LOG.error("json format conversion error in the executeParse() method", e);
		}

	}
	



}
