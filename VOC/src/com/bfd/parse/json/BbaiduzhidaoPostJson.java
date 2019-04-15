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
 * @site 百度知道
 * @function Json插件获取浏览数
 * @author bfd_02
 *
 */
public class BbaiduzhidaoPostJson implements JsonParser{
	private static final Log LOG = LogFactory.getLog(BbaiduzhidaoPostJson.class);
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
				LOG.warn("json :" + json + ".url:" + taskdata.get("url"));
				parsecode = Constants.JSONPROCESS_FAILED;
				LOG.warn(
						"AMJsonParser exception, taskdata url="
								+ taskdata.get("url")
								+ ".jsonUrl :" + data.getUrl(), e);
			}

		}
		JsonParserResult result = new JsonParserResult();
		try {

			result.setData(parsedata);
			result.setParsecode(parsecode);
		} catch (Exception e) {
			LOG.error("jsonparser reprocess error url:"+taskdata.get("url"));
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata,
			String json, String url, ParseUnit unit){
		try {
			if(url.contains("qbpv?q="))
			{
				parsedata.put("view_cnt", json);
			}
			else if(url.contains("comment"))
			{
				Map<String,Object> commentMap = (Map<String, Object>) JsonUtils.parseObject(json);
				if(parsedata.containsKey("best_id"))
				{
					String bestId = (String) parsedata.get("best_id");
					if(url.contains(bestId))
					{
						Map<String, Object> res = (Map<String, Object>) commentMap.get("res");
						parsedata.put("best_replycount", res.get("total_count"));
					}
				}
				if(parsedata.containsKey("replys"))
				{
					List<Map<String, Object>> replys = (List<Map<String, Object>>) parsedata.get("replys");
					for(Map<String, Object> reply : replys)
					{
						if(reply.containsKey("replyid"))
						{
							String replyid = (String) reply.get("replyid");
							if(url.contains(replyid))
							{
								Map<String, Object> res = (Map<String, Object>) commentMap.get("res");
								reply.put("replycount", res.get("total_count"));
							}
						}
					}
				}
			}
		}catch (Exception e) {
			LOG.error("the url is not found");
		}
	}
}
