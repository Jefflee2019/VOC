package com.bfd.parse.json;

import java.util.ArrayList;
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
 * 站点：Npcpop
 * 功能：列表页json处理
 * @author dph 2018年5月28日
 *
 */
public class NpcpopListJson implements JsonParser{
	private static final Log LOG = LogFactory.getLog(NpcpopListJson.class);
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient normalizerClient, ParseUnit unit) {
		Map<String,Object> parsedata = new HashMap<String,Object>(5);
		int parsecode = 0;
		for(Object obj : dataList){
			JsonData data = (JsonData) obj;
			if(!data.downloadSuccess()){
				continue;
			}
			//解压缩ajax数据
			String json = TextUtil.getUnzipJson(data, unit);
			try{
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
						&& (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["),
							json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"),
							json.lastIndexOf("}") + 1);
				}
				// 执行从json数据中提取自己感兴趣的数据
				executeParse(parsedata,json,unit);
			}catch(Exception e){
				LOG.warn("json:" + json + ".url:" + taskdata.get("url"));
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
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private void executeParse(Map<String, Object> parsedata, String json, ParseUnit unit){
	    List<Map<String, Object>> taskList =null;
		if(parsedata.get(Constants.TASKS) != null){
			taskList = (List<Map<String,Object>>) parsedata.get(Constants.TASKS);					
		}else{
			taskList = new ArrayList<Map<String,Object>>();
		}
		List<Map<String, Object>> itemList =null;
		if(parsedata.get(Constants.ITEMS) != null){
			itemList = (List<Map<String,Object>>) parsedata.get(Constants.ITEMS);					
		}else{
			itemList = new ArrayList<Map<String,Object>>();
		}
	    try {
			Map<String, Object> jsonMap = (Map<String, Object>) JsonUtil.parseObject(json);
			List<Map<String, Object>> listMap = (List<Map<String, Object>>) jsonMap.get("list");
			for(Map<String,Object> map : listMap){
				String title = (String) map.get("ArtTitle");
				String time = (String) map.get("ArtPubDate");
				String link = (String) map.get("ArtUrl");
				Map<String, Object> item = new HashMap<String,Object>();
				Map<String, Object> linkMap = new HashMap<String,Object>();
				item.put(Constants.TITLE, title);
				item.put(Constants.POST_TIME, time);
				linkMap.put(Constants.LINK, link);
				linkMap.put(Constants.RAWLINK, link);
				linkMap.put(Constants.LINKTYPE, "newscontent");
				item.put(Constants.LINK, linkMap);
				itemList.add(item);
				taskList.add(linkMap);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    parsedata.put(Constants.ITEMS, itemList);
	    //获取下一页
	    //http://mobile.pcpop.com/pcpop-api/article/articlelist_artset/?channel=5&brand=0&cate=005900050&prop=&subprop=0&size=39&index=1&ispager=1&stype=&grade=0%2C1%2C2%2C3%2C4%2C5%2C6
	    String url = unit.getUrl();
	    Matcher urlM = Pattern.compile("index=(\\d+)").matcher(url);
	    if(urlM.find()){
	    	int num = Integer.parseInt(urlM.group(1));
	    	num = num + 1;
	    	//最多翻5页
	    	if(num < 6){
	    		url = url.replaceAll("index=\\d+", "index=" + num);
	    		Map<String, Object> map= new HashMap<String,Object>();
	    		map.put(Constants.LINK, url);
	    		map.put(Constants.RAWLINK, url);
	    		map.put(Constants.LINKTYPE, "newslist");
	    		parsedata.put(Constants.NEXTPAGE, url);
	    		taskList.add(map);
	    	}
	    }
	    parsedata.put(Constants.TASKS, taskList);
	    
	}

}
