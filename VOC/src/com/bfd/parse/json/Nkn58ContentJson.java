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
 * @site：微客网 -新闻(Nkn58)
 * @function：新闻内容页 Json插件 获取评论页链接
 * 
 * @author bfd_02
 */
public class Nkn58ContentJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(Nkn58ContentJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient normalizerClient,
			ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
		for (JsonData data : dataList) {
			if (!data.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(data, unit);
			try {
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
				LOG.warn("JsonParser exception, taskdata url=" + taskdata.get("url") + ".jsonUrl :" + data.getUrl(), e);
			}
		}
		JsonParserResult result = new JsonParserResult();
		try {
			result.setData(parsedata);
			result.setParsecode(parsecode);
		} catch (Exception e) {
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		try {
			if (json.contains("sid")) {
				List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
				parsedata.put(Constants.TASKS, taskList);
				String sid = findIid(json);
				if(!sid.equals("")) {
				String urlComment = "http://api.v2.uyan.cc/v4/action/?"
						+ sid + "&pn=0";
				Map<String, Object> commentTask = new HashMap<String, Object>();
				commentTask.put(Constants.LINK, urlComment);
				commentTask.put(Constants.RAWLINK, urlComment);
				commentTask.put(Constants.LINKTYPE, "newscomment");
				taskList.add(commentTask);
				
				parsedata.put(Constants.COMMENT_URL, urlComment);
				parsedata.put(Constants.TASKS, taskList);
				}
			}
		} catch (Exception e) {
			LOG.error("excuteParser error url is :" + url);
		}
	}

	private String findIid(String url) {
		//"sid":10396076,
		Pattern iidPatter = Pattern.compile("\"sid\":(\\d+),");
		Matcher match = iidPatter.matcher(url);
		while (match.find()) {
			return match.group(1);
		}
		return null;
	}
}