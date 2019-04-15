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
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * 站点名：Nit168
 * 
 * 主要功能：获取评论数
 * 
 * @author bfd_03
 *
 */
public class Nit168ContentJson implements JsonParser{

	private static final Log LOG = LogFactory.getLog(Nit168ContentJson.class);
	
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient urlnormalizerClients, 
			ParseUnit unit) {
		int parsecode = 0;
		Map<String,Object> parsedata = new HashMap<String,Object>();
		
		// 遍历dataList
		for(Object obj:dataList){
			JsonData data = (JsonData)obj;
			// 判断该ajax数据是否下载成功
			if(!data.downloadSuccess()){
				continue;
			}
			// 解压缩ajax数据
			String json = TextUtil.getUnzipJson(data, unit);
			try{
				// 将ajax数据转化为json数据格式
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
						&& (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["), json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
				}
				// 执行从json数据中提取自己感兴趣的数据
				executeParse(parsedata,json,data.getUrl(),unit);
			}catch(Exception e){
				//e.printStackTrace();
				//LOG.warn("json:" + json + ".url:" + taskdata.get("url"));
				LOG.warn("AMJsonParse exception,taskdat url="+ taskdata.get("url") 
								+ ".jsonUrl:"+ data.getUrl(), e);
			}
		}	
		
		// 组装返回结果
		JsonParserResult result = new JsonParserResult();
		try{
			result.setParsecode(parsecode);	
			result.setData(parsedata);
		}catch(Exception e){
			//e.printStackTrace();
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
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
			//e.printStackTrace();
			LOG.error("json parse error or json is null");
		}
		if (obj instanceof Map) {
			// 评论总数
			Map map = (HashMap) obj;
			if (map.containsKey("response")) {
				Map response = (HashMap) map.get("response");
				Pattern p = Pattern.compile("&threads=([:\\d]+)");
				Matcher m = p.matcher(url);
				String threads = "";
				if (m.find()) {
					threads = m.group(1);
				}
				if (response.containsKey(threads)) {
					Map replyMap = (HashMap) response.get(threads);
					parsedata.put(Constants.REPLY_CNT, replyMap.get("comments")); // 评论总数
				}
			}

		}
		
	}
	
}
