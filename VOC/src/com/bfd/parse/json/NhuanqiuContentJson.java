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
 * @site：环球网 -新闻(Nhuanqiu)
 * @function：新闻内容页 Json插件 获取评论页链接
 * 
 * @author bfd_02
 */
public class NhuanqiuContentJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(NhuanqiuContentJson.class);

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
			if (json.contains("_id")) {
				List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
				parsedata.put(Constants.TASKS, taskList);
				String itemId = findIid(json);
				if(!itemId.equals("")) {
					String urlComment = "http://commentn.huanqiu.com/api/v2/async?a=comment&m=comment_list&sid="
							+ findIid(json) + "&n=15&p=1&appid=e8fcff106c8f";
					Map<String, Object> commentTask = new HashMap<String, Object>();
					commentTask.put(Constants.LINK, urlComment);
					commentTask.put(Constants.RAWLINK, urlComment);
					commentTask.put(Constants.LINKTYPE, "newscomment");
					taskList.add(commentTask);
					
					parsedata.put(Constants.COMMENT_URL, urlComment);
				}
			}
		} catch (Exception e) {
			LOG.error("excuteParser error url is :" + url);
		}
	}

	private String findIid(String url) {
		Pattern iidPatter = Pattern.compile("\"_id\":\"([\\d[a-z]]*)\"");
		Matcher match = iidPatter.matcher(url);
		while (match.find()) {
			return match.group(1);
		}
		return null;
	}
}