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
 * 驱动之家新闻
 * 评论页面动态数据插件
 * 取评论数据
 * @author bfd_05
 *
 */
public class NmydriversCommentJson implements JsonParser{
	private static final Log LOG = LogFactory.getLog(NmydriversCommentJson.class);
	private static final Pattern P = Pattern.compile("<(\\w+)\\s.*>.*</\\1>|<\\w+/>");
	
	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parseData, String json,
			String url, ParseUnit unit) {
		Object obj = null;
		try {
			obj = JsonUtil.parseObject(json);
		} catch (Exception e) {
//			e.printStackTrace();
			LOG.error("jsonparser reprocess error url:" + url);
		}
		List<Object> dataList = new ArrayList<Object>();//存放评论的list
		parseData.put(Constants.COMMENTS, dataList);
		int commtCnt = 0;
		if(obj instanceof Map){
			Map<String, Object> data = (Map<String, Object>) obj;
			List<Map<String, Object>> comList  = (List<Map<String, Object>>)data.get("All");
			for(Map<String, Object> comment : comList){
				initParseData(dataList, comment);
			}
			if(data.containsKey("Count")){
				if(data.get("Count") instanceof List){
					List<Object> list = (List<Object>) data.get("Count");
					if(list.get(0) instanceof Map){
						Map<String, Object> map = (Map<String, Object>) list.get(0);
						if(map.containsKey("ReviewCount")){
							commtCnt = Integer.valueOf((String)map.get("ReviewCount"));
							parseData.put(Constants.REPLY_CNT, map.get("ReviewCount"));
						}
					}
				}
			}
		}
		List<Map<String,Object>> taskList = new ArrayList<Map<String,Object>>();
		parseData.put("tasks", taskList);
		String[] urls = null;
		if(url.contains("&page=")){
			urls = url.split("&page=");
		}
		else {
			urls = url.split("Page=");
		}
		if(urls.length > 1){
			int pageIndex = Integer.valueOf(urls[1]) + 1;
			int totalPage = commtCnt%20 == 0 ? commtCnt/20 : commtCnt/20 + 1;
			if(pageIndex <= totalPage){
				String nextpage = urls[0] + "&page=" + pageIndex;
				Map<String, Object> taskMap = new HashMap<String, Object>();
				taskMap.put("link", nextpage);
				taskMap.put("rawlink", nextpage);
				taskMap.put("linktype", "newscomment");
				taskList.add(taskMap);
				parseData.put("nextpage", taskMap);
				parseData.put("task", taskList);
			}
		}
	}
	
	private void initParseData(List<Object> dataList,
			Map<String, Object> m) {
		Map<String, Object> newMap = new HashMap<String, Object>();
		//评论内容
		if(m.containsKey("Content")){
			Matcher mch = P.matcher((String)m.get("Content"));
			String content = (String) m.get("Content");
			if(mch.find()){
				content = content.replace(mch.group(0).toString(), "");
			}
			newMap.put(Constants.COMMENT_CONTENT, content);
		}
		//顶
		if(m.containsKey("Support")){
			newMap.put(Constants.UP_CNT, m.get("Support"));
		}
		//踩
		if(m.containsKey("Oppose")){
			newMap.put(Constants.DOWN_CNT, m.get("Oppose"));
		}
		//评论时间
		if(m.containsKey("PostDate")){
			newMap.put(Constants.COMMENT_TIME, m.get("PostDate"));
		}
		//评论人名称
		if(m.containsKey("UserName")){
			newMap.put(Constants.USERNAME, m.get("UserName").equals("") ? "游客" : m.get("UserName"));
		}
		//所在地
		if(m.containsKey("IPAdd")){
			newMap.put(Constants.CITY, m.get("IPAdd"));
			
		}
		//楼层
		if(m.containsKey("Floor")) {
			Object floor = m.get("Floor");
			if(floor !=null) {
				newMap.put(Constants.REPLYFLOOR, Integer.parseInt(floor.toString()));
			}
		}
		dataList.add(newMap);
	}

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient arg2, ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parseData = new HashMap<String, Object>();
		// 遍历dataList
		for (JsonData jsonData : dataList) {
			if (!jsonData.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(jsonData, unit);
			try {
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
						&& (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["),
							json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"),
							json.lastIndexOf("}") + 1);
				}
				executeParse(parseData, json, unit.getUrl(), unit);
			} catch (Exception e) {
//				e.printStackTrace();
				parsecode = 500012;
				LOG.warn(
						"AMJsonParser exception, taskdata url="
								+ taskdata.get("url") + ".jsonUrl :"
								+ jsonData.getUrl(), e);
			}
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
