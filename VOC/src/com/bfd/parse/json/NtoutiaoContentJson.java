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
public class NtoutiaoContentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NtoutiaoContentJson.class);
	private static final Pattern PAGE_PATTERN = Pattern.compile("offset=(\\d+)");
	private static final Pattern COUNT_PATTERN = Pattern.compile("count=(\\d+)");

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
//				executeParse(parsedata, json, data.getUrl(), unit);
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
			List<Map<String, Object>> articleList = (List<Map<String, Object>>) jsonMap.get("data");
			int _offset = (int) jsonMap.get("offset");
			List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
			parsedata.put("tasks", taskList);

			if (articleList != null && !articleList.isEmpty()) {
				List<Map<String, Object>> itemList = new ArrayList<Map<String, Object>>();
//				Matcher match = PAGE_PATTERN.matcher(url);
//				Matcher coutnMatch = COUNT_PATTERN.matcher(url);
				for (Map<String, Object> articleItem : articleList) {
					Map<String, Object> reMap = new HashMap<String, Object>();
					Map<String, Object> tempTask = new HashMap<String, Object>();
					if (!articleItem.containsKey("share_url")) {
						continue;
					}
					// 只取今日头条自己的新闻
					String articleLink = articleItem.get("share_url").toString();
					// 网站改版，之前的模板解析不可用，url 中'group/123'需要替换成'a123'
					if (!articleLink.contains("group\\/")) {
						articleLink = articleLink.replace("group/", "a");
					}
					if (!(articleLink.startsWith("http://toutiao.com")
							|| articleLink.startsWith("http://www.toutiao.com") || articleLink
								.startsWith("http://mp.weixin.qq.com"))) {
						continue;
					}

					reMap.put(Constants.ITEMNAME, articleItem.get("title"));

					if (articleLink != null && articleLink.contains("?iid")) {
						reMap.put(Constants.ITEMLINK, articleLink.split("[?]")[0]);
					} else {
						reMap.put(Constants.ITEMLINK, articleLink);
					}

					tempTask.put("link", reMap.get(Constants.ITEMLINK));
					tempTask.put("rawlink", reMap.get(Constants.ITEMLINK));
					tempTask.put("linktype", "newscontent");

					taskList.add(tempTask);
					itemList.add(reMap);
				}
				parsedata.put("items", itemList); // parseResult body
				int oldOffset = Integer.parseInt(this.getCresult(url, "offset=(\\d+)"));
				if (_offset != oldOffset) {
//					int offset = oldOffset + Integer.parseInt(coutnMatch.group(1));
					String nextPage = url.replaceAll("offset=" + oldOffset, "offset=" + String.valueOf(_offset));
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
