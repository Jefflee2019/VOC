package com.bfd.parse.json;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

public class N10jqkaCommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(N10jqkaCommentJson.class);
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient normalizerClient,
			ParseUnit unit) {
		List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
		Map<String, Object> parsedata = new HashMap<String, Object>();
		parsedata.put("tasks", taskList);
		int parsecode = 0;
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
				parsecode = 500012;
				LOG.warn(
						"AMJsonParser exception, taskdata url="
								+ taskdata.get("url") + ".jsonUrl :"
								+ data.getUrl(), e);
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
		Object obj = null;
		try {
			obj = JsonUtil.parseObject(json);
			if (obj instanceof Map) {
				Map data = (Map) obj;
				if (data.containsKey("result")) {
					Map temp = (Map) data.get("result");
					List list = new ArrayList();
					// 评论数
					int replyCnt = 0;
					if (temp.containsKey("amount")) {
						replyCnt = Integer.valueOf(temp.get("amount")
								.toString());
						parsedata.put(Constants.REPLY_CNT, replyCnt);
					}
					if (temp != null && temp.containsKey("reply")) {
						List comments = (List) temp.get("reply");
						for (int i = 0; i < comments.size(); i++) {
							Map comm = new HashMap();
							comm.put(Constants.COMMENT_CONTENT,
									((Map) comments.get(i)).get("content"));
							comm.put(Constants.COMMENTER_NAME,
									((Map) comments.get(i)).get("nickname"));
							comm.put(Constants.UP_CNT,
									((Map) comments.get(i)).get("likes"));
							comm.put(Constants.COMMENT_TIME, timeformat(Long
									.parseLong(((Map) comments.get(i)).get(
											"ctime").toString())));
							list.add(comm);
						}
						parsedata.put(Constants.COMMENTS, list);
					}
				}
			}
		} catch (Exception e) {
			LOG.error(e);
		}
	}
	
	private String timeformat(long time) {
		GregorianCalendar gc = new GregorianCalendar();
//		gc.setTimeInMillis(time);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(gc.getTime());
	}
}
