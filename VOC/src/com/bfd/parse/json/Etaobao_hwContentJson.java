package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * Etaobao_hw获取总评数、好评数、中评数、买家印象、收藏数
 * @author BFD_499
 *
 */
public class Etaobao_hwContentJson implements JsonParser{
	private static final Log LOG = LogFactory.getLog(Etaobao_hwContentJson.class);
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
		List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
		parsedata.put("tasks", taskList);
		Object obj = null;
		try {
			obj = JsonUtil.parseObject(json);
		} catch (Exception e) {
			LOG.error("json format conversion error in parseObject(json) method", e);
		}
		if (obj instanceof Map) {
			Map<String,Object> map = (HashMap<String,Object>) obj;
			//如果url是总评数、买家印象 https://rate.taobao.com/detailCommon.htm?auctionNumId=520019430951&userNumId=1700182551&callback=json_tbc_rate_summary
			if(url.contains("json_tbc_rate_summary"))
			{
				//data{"count":{},"impress":{}}
				if(map.containsKey("data"))
				{
					Map<String, Object> dataMap = (Map<String, Object>) map.get("data");
					Map<String, Object> countMap = (Map<String, Object>) dataMap.get("count");
					int replyCnt = (int) countMap.get("total");
					int goodCnt = (int) countMap.get("good");
					int poorCnt = (int) countMap.get("bad");
					int generalCnt = (int) countMap.get("normal");
					int withpicCnt = (int) countMap.get("pic");
					int againCnt = (int) countMap.get("additional");
					parsedata.put(Constants.REPLY_CNT, replyCnt);
					parsedata.put(Constants.GOOD_CNT, goodCnt);
					parsedata.put(Constants.POOR_CNT, poorCnt);
					parsedata.put(Constants.GENERAL_CNT, generalCnt);
					parsedata.put(Constants.WITHPIC_CNT, withpicCnt);
					parsedata.put(Constants.AGAIN_CNT, againCnt);
					StringBuffer impressStr = new StringBuffer();
					ArrayList<Map<String, Object>> impressList = (ArrayList<Map<String, Object>>) dataMap.get("impress");
					int i = 1;
				    for(Map<String, Object> impress:impressList)
				    {
				    	if(i < impressList.size())
				    	{
				    	    impressStr.append(impress.get("title") + ":" + impress.get("count") + ",");
				    	}
				    	else
				    	{
				    		impressStr.append(impress.get("title") + ":" + impress.get("count"));
				    	}
				    	i++;
				    }
				    parsedata.put(Constants.BUYER_IMPRESSION,impressStr);
				}
			}
			//如果是收藏人数的url
			if(url.contains("count.taobao.com"))
			{
				Iterator it = map.entrySet().iterator();
				while(it.hasNext())
				{
					Map.Entry entry = (Entry) it.next();
					String key = (String) entry.getKey();
					int collectCnt = (int) entry.getValue();
					if(key.contains("ICCP"))
					{
						parsedata.put("collect_cnt",collectCnt);
					}
				}
			}
		}
		/**
		 * 站点评论页需要 Cookie 暂时不给出评论页地址
		 */
//		if (url.contains("rate.taobao.com/feedRateList.htm")) {
//			Map<String, Object> commentTask = new HashMap<String, Object>();
//			commentTask.put(Constants.LINK, url);
//			commentTask.put(Constants.RAWLINK, url);
//			commentTask.put(Constants.LINKTYPE, "eccomment");
//			taskList.add(commentTask);
//			parsedata.put(Constants.COMMENT_URL, commentTask);
//		}
	}
	
}
