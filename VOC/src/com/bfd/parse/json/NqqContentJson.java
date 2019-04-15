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
 * 站点名：腾讯网
 * 
 * 主要功能：获取评论总数
 * 
 * @author bfd_06
 */

public class NqqContentJson implements JsonParser{

	private static final Log LOG = LogFactory.getLog(NqqContentJson.class);
	
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient urlnormalizerClients, 
			ParseUnit unit) {
		int parsecode = 0;
		Map<String,Object> parsedata = new HashMap<String,Object>();
		
		for(Object obj:dataList){
			JsonData data = (JsonData)obj;
			if(!data.downloadSuccess()){
				continue;
			}
			String json = TextUtil.getUnzipJson(data, unit);
			try{
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
						&& (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["), json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
				}
				executeParse(parsedata,json,data.getUrl(),unit);
			}catch(Exception e){
				LOG.error("JsonParse reprocess exception, taskdat url="
						+ taskdata.get("url") + ".jsonUrl:" + data.getUrl(), e);
			}
		}	
		
		// 组装返回结果
		JsonParserResult result = new JsonParserResult();
		try{
			result.setParsecode(parsecode);	
			result.setData(parsedata);
		}catch(Exception e){
			LOG.error("JsonParse reprocess error, taskdat url=" + taskdata.get("url"), e);
		}
		return result;
	}

	@SuppressWarnings("rawtypes")
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		Object obj = null;
		try {
			obj = JsonUtil.parseObject(json);
		} catch (Exception e) {
			LOG.error(
					"json format conversion error in the executeParse() method",
					e);
		}
		if (obj instanceof Map) {
			// REPLY_CNT
			Map map = (HashMap) obj;
			// 判断页面是否含有data字段 防止解析到错误页面   比如http://coral.qq.com/article/38478910/commentnum
			if (map.containsKey("data")) {
				Map dataMap = (HashMap) map.get("data");
				parsedata.put(Constants.REPLY_CNT, dataMap.get("commentnum")); 
			}
		}
	}
}
