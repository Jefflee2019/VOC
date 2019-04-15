package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.TextUtil;

public class BxcarListJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(BxcarListJson.class);
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
				executeParse(parseData, json, jsonData.getUrl(), unit);
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
	private void executeParse(Map<String, Object> parseData, String json, String url, ParseUnit unit) {
		List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
		parseData.put(Constants.TASKS, tasks);
		parseData.put(Constants.ITEMS, items);
		Pattern p = Pattern.compile("/(\\d+)/0$");
		Matcher m = p.matcher(unit.getUrl());
		int pageIndex = 1;
		if(m.find()){
			pageIndex = Integer.valueOf(m.group(1));
		}
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		try {
			jsonMap = (Map<String, Object>) JsonUtils.parseObject(json);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map<String, Object> togetherResult = (Map<String, Object>) jsonMap.get("togetherResult");
		int totalPage = (int) togetherResult.get("totalPage");
		if (totalPage != 0) {
			List<Map<String, Object>> togetherViewList =(List<Map<String, Object>>)togetherResult.get("togetherViewList");
			for (Map<String, Object> map : togetherViewList) {
				String type = (String) map.get("type");
				if ("bbs".equals(type)) {
					//解析论坛数据
					this.parseBBS(parseData, tasks, items, map);
				}
			}
			/**
			 * 判断是否有下一页
			 */
			if (pageIndex < totalPage) {
				Map<String, Object> task = new HashMap<String, Object>();
				int num = pageIndex + 1;
				String nextPageUrl = unit.getUrl().replace("/"+pageIndex+"/0", "/"+num+"/0");
				task.put("link", nextPageUrl);
				task.put("rawlink", nextPageUrl);
				task.put("linktype", "bbspostlist");
				tasks.add(task);
				parseData.put(Constants.NEXTPAGE, task);
			}
		}
		
		
		
		
	}
	
	/**
	 * 
	 * @param parseData 
	 * @param tasks
	 * @param items
	 * @param map
	 */
	private void parseBBS(Map<String, Object> parseData, List<Map<String, Object>> tasks,
			List<Map<String, Object>> items, Map<String, Object> map) {
		Map tmp_task = new HashMap();
		tmp_task.put(Constants.LINK, (String) map.get("post_url"));
		tmp_task.put(Constants.RAWLINK, (String) map.get("post_url"));
		tmp_task.put(Constants.LINKTYPE, "bbspost");
		Map tmp_item = new HashMap();
		tmp_item.put(Constants.ITEMLINK, tmp_task);
		tmp_item.put(Constants.ITEMNAME, ((String) map.get("title")).replaceAll("<em class='red_color'>", "").replaceAll("</em>", ""));
		tmp_item.put(Constants.POSTTIME, (String) map.get("publish_time"));
		tmp_item.put(Constants.REPLY_CNT, (String) map.get("replies"));
		tmp_item.put(Constants.VIEW_CNT, (String) map.get("views"));
		items.add(tmp_item);
		tasks.add(tmp_task);
		parseData.put(Constants.TASKS, tasks);
		parseData.put(Constants.ITEMS, items);
	}


}
