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

/**
 * @site：知乎专栏(Nzhihuzhuanlan)
 * @function：获得列表页
 * @author bfd_02
 *
 */
public class NzhihuzhuanlanListJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NzhihuzhuanlanListJson.class);

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
			Map<String, Object> pagingMap = (Map<String, Object>) jsonMap.get("paging");
			List<Map<String, Object>> taskList = null;
			if (parsedata.get(Constants.TASKS) != null) {
				taskList = (List<Map<String, Object>>) parsedata.get(Constants.TASKS);
			} else {
				taskList = new ArrayList<Map<String, Object>>();
			}
			if (!jsonMap.containsKey("data")) {
				return;
			}
			List<Map<String, Object>> articleList = (List<Map<String, Object>>) jsonMap.get("data");
			if (articleList != null && !articleList.isEmpty()) {
				List<Map<String, Object>> itemList = new ArrayList<Map<String, Object>>();
				for (Map<String, Object> articleMap : articleList) {
					Map<String, Object> reMap = new HashMap<String, Object>();
					Map<String, Object> tempTask = new HashMap<String, Object>();

					String articleLink = articleMap.get("url").toString();
					String articleTitle = articleMap.get("title").toString();
					tempTask.put("link", articleLink);
					tempTask.put("rawlink", articleLink);
					tempTask.put("linktype", "newscontent");

					reMap.put(Constants.LINK, tempTask);
					reMap.put(Constants.TITLE, articleTitle);

					taskList.add(tempTask);
					itemList.add(reMap);
				}
				parsedata.put(Constants.ITEMS, itemList);
				parsedata.put(Constants.TASKS, taskList);
			}

			// 下一页任务
			if (pagingMap.containsKey("totals")) {
				int replyCnt = Integer.parseInt(pagingMap.get("totals").toString().replace(",", ""));
				Matcher match = Pattern.compile("&limit=(\\d+)&offset=(\\d+)").matcher(url);
				if (match.find()) {
					int pagesize = Integer.parseInt(match.group(1));
					int pageno = Integer.parseInt(match.group(2));
					if (pageno < replyCnt) {
						String nextPage = url.replace("&offset=" + pageno, "&offset=" + (pageno + pagesize));
						Map<String, Object> nextpageTask = new HashMap<String, Object>();
						nextpageTask.put(Constants.LINK, nextPage);
						nextpageTask.put(Constants.RAWLINK, nextPage);
						nextpageTask.put(Constants.LINKTYPE, "newslist");
						taskList.add(nextpageTask);
						parsedata.put(Constants.NEXTPAGE, nextpageTask);
						parsedata.put(Constants.TASKS, taskList);
					}
				}
			}
		} catch (Exception e) {
			LOG.error("executeParse error " + url);
		}
	}
}
