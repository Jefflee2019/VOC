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
 * 站点：东方财富网股吧 功能：获取列表页
 * 
 * @author lth 2018年12月3日
 *
 */
public class BeastmoneyListJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(BeastmoneyListJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient normalizerClient, ParseUnit unit) {
		Map<String, Object> parsedata = new HashMap<String, Object>(5);
		int parsecode = 0;
		for (Object obj : dataList) {
			JsonData data = (JsonData) obj;
			if (!data.downloadSuccess()) {
				continue;
			}
			// 解压缩ajax数据
			String json = TextUtil.getUnzipJson(data, unit);
			try {
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0 && (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["), json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
				}
				// 执行从json数据中提取自己感兴趣的数据
				executeParse(parsedata, json, data.getUrl());
			} catch (Exception e) {
				LOG.warn("json:" + json + ".url:" + taskdata.get("url"));
				LOG.warn("AMJsonParse exception,taskdat url=" + taskdata.get("url") + ".jsonUrl:" + data.getUrl(), e);
			}
		}
		// 组装返回结果
		JsonParserResult result = new JsonParserResult();
		try {
			result.setParsecode(parsecode);
			result.setData(parsedata);
		} catch (Exception e) {
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

	/**
	 * 从json中提取信息
	 * 
	 * @param parsedata
	 * @param json
	 */
	@SuppressWarnings({ "unchecked" })
	public void executeParse(Map<String, Object> parsedata, String json, String url) {
		List<Map<String, Object>> taskList = null;
		if (parsedata.get(Constants.TASKS) != null) {
			taskList = (List<Map<String, Object>>) parsedata.get(Constants.TASKS);
		} else {
			taskList = new ArrayList<Map<String, Object>>();
		}
		List<Map<String, Object>> itemList = null;
		if (parsedata.get(Constants.ITEMS) != null) {
			itemList = (List<Map<String, Object>>) parsedata.get(Constants.ITEMS);
		} else {
			itemList = new ArrayList<Map<String, Object>>();
		}
		Object jsonData = null;
		try {
			jsonData = JsonUtil.parseObject(json);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("jsondata fails to download");
		}
		if (jsonData instanceof Map) {
			Map<String, Object> jsonDataMap = (Map<String, Object>) jsonData;
			if (jsonDataMap.containsKey("Data")) {
				List<Map<String, Object>> jsonDataList = (List<Map<String, Object>>) jsonDataMap.get("Data");
				if (jsonDataList != null && !jsonDataList.isEmpty()) {
					for (Map<String, Object> postMap : jsonDataList) {
						Map<String, Object> itemTempMap = new HashMap<String, Object>();
						Map<String, Object> taskTempMap = new HashMap<String, Object>();
						// 帖子主题
						if (postMap.containsKey("Title")) {
							String itemname = postMap.get("Title").toString();
							itemTempMap.put(Constants.ITEMNAME, itemname);
						}

						// 帖子链接
						if (postMap.containsKey("Url")) {
							Map<String, Object> itemlink = new HashMap<String, Object>();
							String posturl = postMap.get("Url").toString();
							itemlink.put("link", posturl);
							itemlink.put("rawlink", posturl);
							itemlink.put("linktype", "bbspost");
							itemTempMap.put("itemlink", itemlink);
							taskTempMap.put("link", posturl);
							taskTempMap.put("rawlink", posturl);
							taskTempMap.put("linktype", "bbspost");
						}

						// 发表时间
						if (postMap.containsKey("CreateTime")) {
							String posttime = postMap.get("CreateTime").toString();
							itemTempMap.put(Constants.POSTTIME, posttime);
						}

						// 回复数
						itemTempMap.put(Constants.REPLY_CNT, -1024);

						// 添加到items
						itemList.add(itemTempMap);
						parsedata.put(Constants.ITEMS, itemList);
						// 添加任务到tasks
						taskList.add(taskTempMap);
						parsedata.put(Constants.TASKS, taskList);
					}
				}
			}
			//下一页任务
			if (jsonDataMap.containsKey("TotalPage")) {
				// 总页数
				int totalPageNo = Integer.parseInt(jsonDataMap.get("TotalPage").toString());
				// 当前页码
				// http://api.so.eastmoney.com/bussiness/Web/GetSearchList?type=701&pageindex=1&pagesize=10&keyword=%E5%8D%8E%E4%B8%BA
				String nextpage = null;
				int currentpage = 0;
				Matcher match = Pattern.compile("&pageindex=(\\d+)").matcher(url);
				if (match.find()) {
					int currentPageNo = Integer.parseInt(match.group(1));
					nextpage = url.replace("&pageindex=" + currentPageNo , "&pageindex=" + (currentPageNo + 1));
				} else {
					nextpage = url.concat("&pageindex=2");
				}

				if (totalPageNo > currentpage) {
					Map<String, Object> nextpageMap = new HashMap<String, Object>(4);
					nextpageMap.put(Constants.LINK, nextpage);
					nextpageMap.put(Constants.RAWLINK, nextpage);
					nextpageMap.put(Constants.LINKTYPE, "bbspostlist");
					taskList.add(nextpageMap);
					parsedata.put(Constants.NEXTPAGE, nextpageMap);
					parsedata.put(Constants.TASKS, taskList);
				}
			}
		}
	}
}
