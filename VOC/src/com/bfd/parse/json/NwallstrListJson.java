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
 * @site:华尔街见闻(Nwallstr)
 * @function:列表页第二页开始是动态请求，需要json插件获取数据及下一页
 * @author bfd_02
 *
 */

public class NwallstrListJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NwallstrListJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient normalizerClient, ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parseData = new HashMap<String, Object>();
		// 遍历dataList
		for (JsonData jsonData : dataList) {
			if (!jsonData.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(jsonData, unit);
			try {
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0 && (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["), json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
				}
				executeParse(parseData, json, jsonData.getUrl());
			} catch (Exception e) {
				parsecode = 500012;
				LOG.warn(
						"JsonParser exception, taskdata url=" + taskdata.get("url") + ".jsonUrl :" + jsonData.getUrl(),
						e);
			}
		}
		// 组装返回结果
		JsonParserResult result = new JsonParserResult();
		try {
			result.setParsecode(parsecode);
			result.setData(parseData);
		} catch (Exception e) {
			LOG.error("jsonparser  error url:" + taskdata.get("url"));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private void executeParse(Map<String, Object> parseData, String json, String url) {
		List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
		parseData.put(Constants.TASKS, tasks);
		parseData.put(Constants.ITEMS, items);
		try {
			Object jsonObj = JsonUtil.parseObject(json);
			if (jsonObj instanceof Map) {
				Map<String, Object> map = (Map<String, Object>) jsonObj;
				if (map.containsKey("data")) {
					Map<String, Object> dataMap = (Map<String, Object>) map.get("data");
					// 下一页页码，用于判断生成下一页任务
					// https://api-prod.wallstreetcn.com/apiv1/search/article?query=华为&order_type=time&cursor=2&limit=20
//					if (dataMap.containsKey("next_cursor")) {
//						getNextpage(parseData, url, tasks, dataMap);
//					}
					// 用于组装需要参数的map
					// 新闻内容
					if (dataMap.containsKey("items")) {
						List<Map<String, Object>> itemsList = (List<Map<String, Object>>) dataMap.get("items");
						if (!itemsList.isEmpty()) {
							for (Map<String, Object> itemsMap : itemsList) {
								Map<String, Object> itemMap = new HashMap<String, Object>();
								Map<String, Object> link = new HashMap<String, Object>();
								if (itemsMap.containsKey("title")) {
									String title = itemsMap.get("title").toString();
									itemMap.put(Constants.TITLE, title);
								}
								if (itemsMap.containsKey("uri")) {
									String newsUrl = itemsMap.get("uri").toString();
									link.put("link", newsUrl);
									link.put("rawlink", newsUrl);
									link.put("linktype", "newscontent");
									itemMap.put(Constants.LINK, link);
								}
								items.add(itemMap);
								tasks.add(link);
							}
							// 下一页页码，用于判断生成下一页任务
							Matcher pageM = Pattern.compile("cursor=(\\d+)").matcher(url);
							int page = 1;
							if(pageM.find()){
								page = Integer.parseInt(pageM.group(1));
								
							}
							if(page <50){
								getNextpage(parseData, url, tasks, dataMap);
							}
							
						}
					}
				}
			}
		} catch (Exception e) {
			LOG.warn("json parse error,and url is"+url);
		}

	}

	private void getNextpage(Map<String, Object> parseData, String url, List<Map<String, Object>> tasks,
			Map<String, Object> dataMap) {
		String nextCursor = (dataMap.get("next_cursor")).toString();
		if (!nextCursor.equals("")) {
			Matcher match = Pattern.compile("cursor=(\\d+)").matcher(url);
			if (match.find()) {
				int pageid = Integer.parseInt(match.group(1));
				String nextPage = url.replace("cursor=" + pageid, "cursor=" + (pageid + 1));
				Map<String, Object> nextMap = new HashMap<String, Object>();
				nextMap.put(Constants.LINK, nextPage);
				nextMap.put(Constants.RAWLINK, nextPage);
				nextMap.put(Constants.LINKTYPE, "newslist");
				tasks.add(nextMap);
				parseData.put(Constants.NEXTPAGE,nextMap);
			}
		}
	}
}
