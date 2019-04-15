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
 * @site：Ntoutiao
 * @function：获得列表页
 * @author bfd_04
 *
 */
public class NbilibiliListJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NbilibiliListJson.class);

	// private static final String
	// URL_HEAD="http://toutiao.com/search_content/?offset=";
	// private static final String URL_TAIL =
	// "&format=json&keyword=%E5%8D%8E%E4%B8%BA&autoload=true&count=20";
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
			List<Map<String, Object>> result = (List<Map<String, Object>>) jsonMap.get("result");
			int numPages = (int) jsonMap.get("numPages");
			int page = (int) jsonMap.get("page");
			List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
			parsedata.put("tasks", taskList);

			if (result != null && !result.isEmpty()) {
				List<Map<String, Object>> itemList = new ArrayList<Map<String, Object>>();
				for (Map<String, Object> resultItem : result) {
					Map<String, Object> reMap = new HashMap<String, Object>();
					Map<String, Object> tempTask = new HashMap<String, Object>();
					if (!resultItem.containsKey("arcurl")) {
						continue;
					}
					String arcurl = resultItem.get("arcurl").toString();
					reMap.put(Constants.ITEMLINK, arcurl);
					reMap.put(Constants.ITEMNAME, resultItem.get("title"));

					tempTask.put("link", arcurl);
					tempTask.put("rawlink", arcurl);
					tempTask.put("linktype", "newscontent");

					taskList.add(tempTask);
					itemList.add(reMap);
				}
				parsedata.put("items", itemList); // parseResult body
				if (page < numPages) {
//					https://search.bilibili.com/api/search?search_type=video&keyword=%E5%8D%8E%E4%B8%BA&from_source=banner_search&spm_id_from=333.338.banner_link.1&page=1&order=pubdate
					String nextPage = url.replaceAll("&page=" + page, "&page=" + (page + 1));
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
			// e.printStackTrace();
			LOG.error("executeParse error " + url);
		}
	}
	
	/**
	 * 正则匹配字符串
	 * @param str
	 * @param pattern
	 * @return
	 */
	private String getCresult(String str,String reg){
		Pattern pattern = Pattern.compile(reg);
		Matcher mch = pattern.matcher(str);
		if(mch.find()){
			return mch.group(1);
		}
		return str;
	}

}
