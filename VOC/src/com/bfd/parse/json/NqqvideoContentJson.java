package com.bfd.parse.json;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
 * 站点：腾讯视频
 * 
 * @author bfd_01
 */
public class NqqvideoContentJson implements JsonParser {
	private final static Log LOG = LogFactory.getLog(NqqvideoContentJson.class);

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
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0 && (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["), json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
				}
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
				LOG.error("executeParse() exception",e);
			}

		}
		JsonParserResult result = new JsonParserResult();
		try {
			result.setData(parsedata);
			result.setParsecode(parseCode);
		} catch (Exception e) {
			LOG.error("result set exception",e);
		}
		return result;
	}

	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		try {
			String commentid = JsonUtils
					.parseObject(json).get("comment_id").toString();
			List<Map<String, Object>> rtasks = new ArrayList<Map<String, Object>>();
			parsedata.put(Constants.TASKS, rtasks);
			/**
			 * 生成评论任务
			 */
			String commentUrl = null;
			// https://coral.qq.com/article/2128893497/comment?commentid=0&reqnum=10
			commentUrl = "https://coral.qq.com/article/" + commentid + "/comment?commentid=0&reqnum=10";
			if (commentUrl != null) {
				Map<String, Object> commentTask = new HashMap<String, Object>();
				commentTask.put("link", commentUrl);
				commentTask.put("rawlink", commentUrl);
				commentTask.put("linktype", "newscomment");
				parsedata.put(Constants.COMMENT_URL, commentUrl);
				rtasks.add(commentTask);
			}
		} catch (Exception e) {
			LOG.error("executeParse() error " + url);
		}
	}
}
