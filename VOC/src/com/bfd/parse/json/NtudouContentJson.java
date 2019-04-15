package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.DataUtil;
import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * 站点名：土豆网
 * 
 * 功能：动态获取点赞数 评论数 播放总数 并给出评论页地址
 * 
 * @author bfd_06
 * 
 */
public class NtudouContentJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(NtudouContentJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient urlnormalizerClients,
			ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();

		for (Object obj : dataList) {
			JsonData data = (JsonData) obj;
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
				LOG.error("JsonParse reprocess exception, taskdat url="
						+ taskdata.get("url") + ".jsonUrl:" + data.getUrl(), e);
			}
		}

		JsonParserResult result = new JsonParserResult();
		try {
			result.setParsecode(parsecode);
			result.setData(parsedata);
		} catch (Exception e) {
			LOG.error(
					"JsonParse reprocess error, taskdat url="
							+ taskdata.get("url"), e);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		/**
		 * 加上tasks
		 */
		List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
		parsedata.put("tasks", tasks);
		/**
		 * 动态获取字段
		 */
		if (url.contains("iabcdefg")) {
			try {
				Map<String, String> result = (Map<String, String>) JsonUtil
						.parseObject(json);
				// REPLY_CNT
				parsedata.put(Constants.REPLY_CNT, result.get("commentNum"));
				// FAVOR_CNT
				parsedata.put(Constants.FAVOR_CNT, result.get("digNum"));
				// PLAY_CNT
				parsedata.put(Constants.PLAY_CNT, result.get("playNum"));
			} catch (Exception e) {
				LOG.error(
						"json format conversion error in the executeParse() method",
						e);
			}

			/**
			 * 添加评论页链接
			 */
		} else {
			String pageIid = match("iid: (\\d+)", json);
			if (pageIid != null) {
				Map<String, Object> rtask = new HashMap<String, Object>();
				StringBuilder commentUrl = new StringBuilder(
						"http://www.tudou.com/comments/itemnewcomment.srv?jsoncallback=module_data_comment_get&iid=");
				commentUrl
						.append(pageIid)
						.append("&page=1&rows=50&cmtid=0&method=getHotCmt&charset=utf-8&app=anchor");
				rtask.put("link", commentUrl);
				rtask.put("rawlink", commentUrl);
				rtask.put("linktype", "newscomment");
				rtask.put("iid", DataUtil.calcMD5(commentUrl + ""));
				parsedata.put(Constants.COMMENT_URL, commentUrl);
				tasks.add(rtask);
			}
		}

		// LOG.info("url:" + url + " The processing result is "
		// + JsonUtils.toJSONString(parsedata));

	}

	public String match(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return matcher.group(1);
		}

		return null;
	}

}
