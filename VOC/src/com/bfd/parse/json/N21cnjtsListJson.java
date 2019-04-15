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
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.TextUtil;

/**
 * @site：N21cnjts
 * @function：获得列表页
 * @author bfd_02
 *
 */
public class N21cnjtsListJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(N21cnjtsListJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient normalizerClient, ParseUnit unit) {
		int parseCode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
		for (JsonData data : dataList) {
			if (!data.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(data, unit);
			try {
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0 && (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["), json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
				}
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
				// e.printStackTrace();
				LOG.warn("json :" + json + ".url:" + taskdata.get("url"));
				LOG.info("goodsList exception during executeParse");
				LOG.warn("AMJsonParser exception, taskdata url=" + taskdata.get("url") + ".jsonUrl :" + data.getUrl(),
						e);
			}

		}
		JsonParserResult result = new JsonParserResult();
		try {
			result.setData(parsedata);
			result.setParsecode(parseCode);
		} catch (Exception e) {
			// e.printStackTrace();
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

	/**
	 * execute parse
	 * 
	 * @param parsedata
	 * @param json
	 * @param url
	 * @param unit
	 */
	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata, String json, String url, ParseUnit unit) {
		try {
			Map<String, Object> jsonMap = (Map<String, Object>) JsonUtils.parseObject(json);
			List<Map<String, Object>> postList = (List<Map<String, Object>>) jsonMap.get("postList");
			List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
			parsedata.put("tasks", taskList);

			if (postList != null && !postList.isEmpty()) {
				List<Map<String, Object>> itemList = new ArrayList<Map<String, Object>>();
				for (Map<String, Object> articleItem : postList) {
					Map<String, Object> reMap = new HashMap<String, Object>();
					Map<String, Object> linkMap = new HashMap<String, Object>();
					Map<String, Object> tempTask = new HashMap<String, Object>();
					if (!articleItem.containsKey("id")) {
						continue;
					}
					String id = articleItem.get("id").toString();
					String articleLink = "http://ts.21cn.com/tousu/show/id/" + id;
					
					//组装items
					linkMap.put("link", articleLink);
					linkMap.put("rawlink", articleLink);
					linkMap.put("linktype", "newscontent");
					reMap.put("title", articleItem.get("title"));
					reMap.put("link", linkMap);
					itemList.add(reMap);
					//组装tasks
					tempTask.put("link", articleLink);
					tempTask.put("rawlink", articleLink);
					tempTask.put("linktype", "newscontent");
					taskList.add(tempTask);

				}
				// http://ts.21cn.com/front/api/search/searchPostList.do?pageNo=2&title=%E5%8D%8E%E4%B8%BA&listType=1
				parsedata.put("items", itemList); // parseResult body
				int nextpageIndex = postList.size();
				int pageno = Integer.parseInt(this.getCresult(url, "pageNo=(\\d+)&"));
				// 如果列表页的新闻数等于10，默认有下一页，提供下一页任务
				if (nextpageIndex == 10) {
					String nextPage = url.replaceAll("pageNo=" + pageno, "pageNo=" + (pageno + 1));
					Map<String, Object> nextpageTask = new HashMap<String, Object>();
					nextpageTask.put("link", nextPage);
					nextpageTask.put("rawlink", nextPage);
					nextpageTask.put("linktype", "newslist");
					taskList.add(nextpageTask);
					parsedata.put("nextpage", nextpageTask);
					parsedata.put("tasks", taskList);
				}
			}
		} catch (Exception e) {
			LOG.error("executeParse error " + url);
		}
	}

	/**
	 * 正则匹配字符串
	 * 
	 * @param str
	 * @param pattern
	 * @return
	 */
	private String getCresult(String str, String reg) {
		Pattern pattern = Pattern.compile(reg);
		Matcher mch = pattern.matcher(str);
		if (mch.find()) {
			return mch.group(1);
		}
		return str;
	}

}
