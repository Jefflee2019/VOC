package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.entity.Constants;
import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * 站点名：Nyidianzixun
 * 
 * 动态解析列表页
 * 
 * @author bfd_06
 * 
 */
public class NyidianzixunListJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(NyidianzixunListJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient urlnormalizerClients, ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
		/**
		 * JsonData为List的原因为jsEngine有时会请求好几个链接
		 */
		for (Object obj : dataList) {
			JsonData data = (JsonData) obj;
			if (!data.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(data, unit);
			executeParse(parsedata, json, data.getUrl(), unit);
		}
		JsonParserResult result = new JsonParserResult();
		result.setParsecode(parsecode);
		result.setData(parsedata);
		return result;
	}

	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata, String json, String url, ParseUnit unit) {
		/**
		 * http://www.yidianzixun.com/home/q/news_list_for_keyword?display=%E5%
		 * 8D%8E%E4%B8%BA&cstart=0&cend=10&word_type=token 组装items和tasks
		 */
		List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
		parsedata.put("tasks", tasks);
		try {
			Map<String, Object> result = (Map<String, Object>) JsonUtil.parseObject(json);
			List<Map<String, Object>> resultList = (List<Map<String, Object>>) result.get("result");
			List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
			if (resultList.size() > 0) {
				for (Map<String, Object> singleNew : resultList) {
					if (singleNew.containsKey("title")) {
						Map<String, Object> linkMap = new HashMap<String, Object>();
						Map<String, Object> item = new HashMap<String, Object>();
						String title = singleNew.get("title").toString();
						if (singleNew.containsKey("docid")) {
							// 内容页url需要拼接
							String docid = singleNew.get("docid").toString();
							StringBuffer sb = new StringBuffer();
							String contentUrl = sb.append("http://www.yidianzixun.com/article/").append(docid)
									.toString();
							linkMap.put(Constants.LINK, contentUrl);
							linkMap.put(Constants.RAWLINK, contentUrl);
							linkMap.put(Constants.LINKTYPE, "newscontent");
							item.put(Constants.TITLE, title);
							item.put(Constants.LINK, linkMap);
							items.add(item);
							tasks.add(linkMap);
						}
					}
				}
			}
			parsedata.put("items", items);
			/**
			 * 如果返回结果大于等于10则给出下一页链接且限定列表页最大翻页为100页
			 */
			if (resultList.size() >= 10) {
				int cstart = matchCstart("cstart=(\\d+)", url);
				String nextUrl = url.replace("cstart=" + cstart + "&cend=" + (cstart + 10), "cstart=" + (cstart + 10)
						+ "&cend=" + (cstart + 20));
				addNextUrl(parsedata, nextUrl);
			}
		} catch (Exception e) {
			LOG.error("json format conversion error in the executeParse() method", e);
		}

	}

	public int matchCstart(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return Integer.parseInt(matcher.group(1));
		}
		return 0;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addNextUrl(Map<String, Object> parsedata, String nextUrl) {
		Map<String, Object> task = new HashMap<String, Object>();
		task.put(Constants.LINK, nextUrl);
		task.put(Constants.RAWLINK, nextUrl);
		task.put(Constants.LINKTYPE, "newslist");
		parsedata.put(Constants.NEXTPAGE, task);
		List<Map> tasks = (List<Map>) parsedata.get("tasks");
		tasks.add(task);
	}

}
