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

public class NenetContentJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(NenetContentJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient normalizerClient, ParseUnit unit) {
		Map<String, Object> parsedata = new HashMap<String, Object>();
		List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
		parsedata.put("tasks", taskList);
		int parsecode = 0;
		for (JsonData data : dataList) {
			if (!data.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(data, unit);
			try {
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
				parsecode = 500012;
				LOG.warn("AMJsonParser exception, taskdata url=" + taskdata.get("url") + ".jsonUrl :" + data.getUrl(),
						e);
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

	public void executeParse(Map<String, Object> parsedata, String json, String url, ParseUnit unit) {
		try {
			String obj = JsonUtil.parseObject(json).toString();
			// <a
			// href="http://cmt.enet.com.cn/main/comInfo.php?mid=8fa72ab899bd9785ce56f86b33b3f991&site=enews"
			// target="_blank">【已有<span>19</span>条评论
			String regex = "<a\\s*href=\"(\\S*)\"\\s*target=\"_blank\">【已有<span>(\\d+)</span>";
			if (obj.contains("【已有<span>")) {
				Matcher match = Pattern.compile(regex).matcher(obj);
				if (match.find()) {
					String commUrl = match.group(1).trim();
					int replyCnt = Integer.parseInt(match.group(2));

					// 拼接评论页链接
					if (replyCnt > 0) {
						List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
						Map<String, Object> commentTask = new HashMap<String, Object>();
						commentTask.put(Constants.LINK, commUrl);
						commentTask.put(Constants.RAWLINK, commUrl);
						commentTask.put(Constants.LINKTYPE, "newscontent");
						taskList.add(commentTask);
						parsedata.put(Constants.COMMENT_URL, commUrl);
						parsedata.put(Constants.TASKS, taskList);
					}
				}
			}
		} catch (Exception e) {
			LOG.error("excuteparse error");
		}
	}
}
