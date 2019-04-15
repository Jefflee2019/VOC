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
 * @site：新浪网-新闻(Nsina)
 * @function：新闻内容页 Json插件 获取评论总数
 * 
 * @author bfd_02
 */
public class NsinaContentJson implements JsonParser {
	private final static Log LOG = LogFactory.getLog(NsinaContentJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient normalizerClient, ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
		for (JsonData data : dataList) {
			// 判断该ajax数据是否下载成功
			if (!data.downloadSuccess()) {
				continue;
			}
			// 解压缩ajax数据
			String json = TextUtil.getUnzipJson(data, unit);
			try {
				// 将ajax数据转化为json数据格式
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0 && (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["), json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
				}
				// 执行从json数据中提取自己感兴趣的数据
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
				LOG.warn("AMJsonParser exception, taskdata url=" + taskdata.get("url") + ".jsonUrl :" + data.getUrl(),
						e);
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
		Object obj = null;
		try {
			obj = JsonUtil.parseObject(json);
		} catch (Exception e) {
			LOG.error("excuteParse error");
		}
		//2018-07-14 因为消重，内容页只会抓取一次，内容页中不在提供评论数等需要实时更新的数据
		if (obj instanceof Map) {
			Map<String, Object> data = (Map<String, Object>) obj;
			if (data.containsKey("result")) {
				Map<String, Object> temp = (Map<String, Object>) data.get("result");
				/**
				 * @param 评论数   REPLY_CNT
				 *          
				 */
				if (temp.containsKey("count")) {
					Map<String, Object> count = (Map<String, Object>) temp.get("count");
					if (count != null && count.containsKey("total")) {
						int total = Integer.parseInt(count.get("total").toString());
						parsedata.put(Constants.REPLY_CNT, total);
					}
				}
			}
		}
	}
}