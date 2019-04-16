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
import com.bfd.parse.util.TextUtil;
/**
 * 站点：Nit168
 * 功能：处理列表页
 * @author dph 2018年6月6日
 *
 */
public class Nit168ListJson implements JsonParser{

	private static final Log LOG = LogFactory.getLog(Nit168ListJson.class);
	
	private static final Pattern ITEM =  Pattern.compile("</a></em><a(\\s|\\S){0,200}</a></div>");
	private static final Pattern LINK =  Pattern.compile("href=\"(\\S+)\"");
	private static final Pattern TITLE =  Pattern.compile("\">(\\s|\\S)*");
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
			executeParse(parseData, json, unit.getUrl());
			try {
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
						&& (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["),
							json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"),
							json.lastIndexOf("}") + 1);
				}
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
	
	private void executeParse(Map<String, Object> parseData, String json, String url) {
		List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
		Matcher itemM = ITEM.matcher(json);
		while(itemM.find()){
			String item = itemM.group(0);
			Matcher linkM = LINK.matcher(item);
			String link = null;
			if(linkM.find()){
				link = linkM.group(1);
			}
			Matcher titleM = TITLE.matcher(item);
			String title = null;
			if(titleM.find()){
				title = titleM.group(0);
				title = title.replace("\">", "").replace("</a></div>", "").trim();
			}
			Map<String,Object> itemMap = new HashMap<String,Object>();
			itemMap.put(Constants.TITLE, title);
			Map<String,Object> linkMap = new HashMap<String,Object>();
			linkMap.put(Constants.LINK, link);
			linkMap.put(Constants.RAWLINK, link);
			linkMap.put(Constants.LINKTYPE, "newscontent");
			itemMap.put(Constants.LINK, linkMap);
			items.add(itemMap);
			tasks.add(linkMap);
		}
		Matcher pageNumM = Pattern.compile("&pageNum=(\\d+)").matcher(url);
		int pageNum = 0;
		if(pageNumM.find()){
			pageNum = Integer.parseInt(pageNumM.group(1));
			
		}
		if(pageNum < 5){
			pageNum ++;
			url = url.replaceAll("&pageNum=\\d+", "&pageNum=" + pageNum);
		}
		Map<String,Object> linkMap = new HashMap<String,Object>();
		linkMap.put(Constants.LINK, url);
		linkMap.put(Constants.RAWLINK, url);
		linkMap.put(Constants.LINKTYPE, "newslist");
		tasks.add(linkMap);
		parseData.put(Constants.TASKS, tasks);
		parseData.put(Constants.ITEMS, items);
		parseData.put(Constants.NEXTPAGE, linkMap);
		
	}

}
