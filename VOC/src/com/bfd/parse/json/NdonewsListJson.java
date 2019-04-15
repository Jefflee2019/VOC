package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.DataUtil;
import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

public class NdonewsListJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NdonewsListJson.class);
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient normalizerClient,
			ParseUnit unit) {
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
				executeParse(parseData, json, jsonData.getUrl());
			} catch (Exception e) {
				parsecode = 500012;
				LOG.warn(
						"JsonParser exception, taskdata url="
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
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private void executeParse(Map<String, Object> parseData, String json, String url) {
		List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
		parseData.put(Constants.TASKS, tasks);
		parseData.put(Constants.ITEMS, items);
		Pattern p = Pattern.compile("page=(\\d+)");
		Matcher m = p.matcher(url);
		int pageIndex = 1;
		if(m.find()){
			pageIndex = Integer.valueOf(m.group(1));
		}
			Object jsonObj = null;
			try {
				jsonObj = JsonUtil.parseObject(json);
				if (jsonObj instanceof Map) {
					Map<String, Object> map = (Map<String, Object>) jsonObj;
					if (map.containsKey("matches")) {
						List<Map<String, Object>> dataList = (List<Map<String, Object>>) map
								.get("matches");
						for (Map<String, Object> dataMap : dataList) {
							Map<String, Object> itemMap = new HashMap<String, Object>();
							if (dataMap.containsKey("attrs")
									&& ((Map) dataMap.get("attrs"))
											.containsKey("title")) {
								itemMap.put(Constants.TITLE, ((Map) dataMap
										.get("attrs")).get("title"));
							}
							if (dataMap.containsKey("attrs")
									&& ((Map) dataMap.get("attrs"))
											.containsKey("url")) {
								String itemUrl = ((Map) dataMap.get("attrs"))
										.get("url").toString();
								handleTask(tasks, items, itemMap, itemUrl);
							}
						}
						if (pageIndex < 50) {
							getNextPage(pageIndex, 50, tasks, parseData, url);
						}
					}
				}
			} catch (Exception e1) {
				LOG.error("NdonewsListJson error url:" + url);
			}
	}

	private void handleTask(List<Map<String, Object>> tasks,
			List<Map<String, Object>> items, Map<String, Object> itemMap,
			String itemUrl) {
		Map<String, Object> link = new HashMap<String, Object>();
		link.put("link", itemUrl);
		link.put("rawlink", itemUrl);
		link.put("linktype", "newscontent");
		itemMap.put(Constants.LINK, link);
		items.add(itemMap);
		
		Map<String, Object> taskMap = new HashMap<String, Object>();
		taskMap.put("iid", DataUtil.calcMD5(itemUrl));
		taskMap.put("link", itemUrl);
		taskMap.put("rawlink", itemUrl);
		taskMap.put("linktype", "newscontent");
		tasks.add(taskMap);
	}
	
	private void getNextPage(int pageIndex, int totalPage, List<Map<String, Object>> tasks, 
			Map<String, Object> parseData, String url){
		if(pageIndex < totalPage){
			Map<String, Object> nextpageMap = new HashMap<String, Object>();
			StringBuilder nextpage = new StringBuilder();
			if(url.split("page=").length > 1){
				nextpage.append(url.split("page=")[0]).append("page=");
			}
			else nextpage.append(url).append("&page=");
			nextpage.append(pageIndex + 1);
			nextpageMap.put("link", nextpage.toString());
			nextpageMap.put("rawlink", nextpage.toString());
			nextpageMap.put("linktype", "newslist");
			nextpageMap.put("iid", DataUtil.calcMD5(nextpage.toString()));
			tasks.add(nextpageMap);
			parseData.put(Constants.NEXTPAGE, nextpageMap);
		}
	}
}
