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
 * 	@site：Nyicai
 * 	@function：获得列表页
 *  API链接:http://www.yicai.com/api/search/?start=0&pagecount=10&type=2&contenttype=0&searchKeyWords=%25E5%258D%258E%25E4%25B8%25BA
 *  @author bfd_01
 *
 */
public class NyicaiListJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NyicaiListJson.class);
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient normalizerClient,
			ParseUnit unit) {
		int parseCode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
		for (JsonData data : dataList) {
			if (!data.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(data, unit);
			try {
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
						&& (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["),
							json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"),
							json.lastIndexOf("}") + 1);
				}
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
//				e.printStackTrace();
				LOG.warn("json :" + json + ".url:" + taskdata.get("url"));
				LOG.info("goodsList exception during executeParse");
				LOG.warn(
						"AMJsonParser exception, taskdata url="
								+ taskdata.get("url")
								+ ".jsonUrl :" + data.getUrl(), e);
			}

		}
		JsonParserResult result = new JsonParserResult();
		try {
			result.setData(parsedata);
			result.setParsecode(parseCode);
		} catch (Exception e) {
//			e.printStackTrace();
			LOG.error("jsonparser reprocess error url:"+taskdata.get("url"));
		}
		return result;
	}

	/**
	 * execute parse
	 * @param parsedata
	 * @param json
	 * @param url
	 * @param unit
	 */
	public void executeParse(Map<String, Object> parsedata,
			String json, String url, ParseUnit unit){
		try {
			Map<String,Object> jsonMap = (Map<String, Object>) JsonUtils.parseObject(json);
			List<Map<String, Object>> list =(List<Map<String, Object>>)jsonMap
					.get("result");
			
			List<Map<String,Object>> taskList = new ArrayList<Map<String,Object>>();
			parsedata.put("tasks", taskList);
			List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
			if(list !=null && !list.isEmpty()) {
				for (int i=0;i<list.size();i++) {
					Map map = list.get(i);
					String title = map.get("title").toString();
					String link = map.get("url").toString();
					Map<String, Object> linkMap = new HashMap<String, Object>();
					Map<String, Object> item = new HashMap<String, Object>();
						linkMap.put(Constants.LINK, link);
						linkMap.put(Constants.RAWLINK, link);
						linkMap.put(Constants.LINKTYPE, "newscontent");
						item.put(Constants.TITLE, title);
						item.put(Constants.LINK, linkMap);
						items.add(item);
						taskList.add(linkMap);
					}
				}
			parsedata.put("items", items);
			// 处理下一页
			String nextUrl = null;
			String newscount = null;
			newscount = jsonMap.get("newscount").toString();
			int count = Integer.valueOf(newscount);
			int pageNum = count/10 + 1;
			if (pageNum > getPageNo(url) +1 ) {
				nextUrl = getNextUrl(url);
				addNextUrl(parsedata, nextUrl);
			}
		} catch (Exception e) {
//			e.printStackTrace();
			LOG.error("executeParse error "+url);
		}
	}

	private void addNextUrl(Map<String, Object> parsedata, String nextUrl) {
		Map<String, Object> task = new HashMap<String, Object>();
		task.put(Constants.LINK, nextUrl);
		task.put(Constants.RAWLINK, nextUrl);
		task.put(Constants.LINKTYPE, "newslist");
		parsedata.put(Constants.NEXTPAGE, task);
		List<Map> tasks = (List<Map>) parsedata.get("tasks");
		tasks.add(task);
	}
	
	private int getPageNo(String url) {
		Pattern p = Pattern.compile("start=(\\d+)&");
		Matcher m = p.matcher(url);
		int pageNo = 0;
		while (m.find()) {
			pageNo = Integer.valueOf(m.group(1));
		}
		return pageNo;
	}
	private String getNextUrl(String url) {
		String nextUrl = null;
		int pageNo = getPageNo(url);
		nextUrl = url.replace("start=" + pageNo, "start=" + (pageNo +1));
		return nextUrl;
	}
}
