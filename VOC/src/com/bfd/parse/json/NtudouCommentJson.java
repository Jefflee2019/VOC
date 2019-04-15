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
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * 站点名：土豆网
 * 
 * 功能：分离评论
 * 
 * @author bfd_06
 * 
 */
public class NtudouCommentJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(NtudouCommentJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient urlnormalizerClients, ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();

		for (Object obj : dataList) {
			JsonData data = (JsonData) obj;
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
				LOG.error(
						"JsonParse reprocess exception, taskdat url=" + taskdata.get("url") + ".jsonUrl:"
								+ data.getUrl(), e);
			}
		}

		JsonParserResult result = new JsonParserResult();
		try {
			result.setParsecode(parsecode);
			result.setData(parsedata);
		} catch (Exception e) {
			LOG.error("JsonParse reprocess error, taskdat url=" + taskdata.get("url"), e);
		}
		return result;
	}

	@SuppressWarnings({ "unchecked" })
	public void executeParse(Map<String, Object> parsedata, String json, String url, ParseUnit unit) {

		try {
			Map<Object, Object> jsonData = (Map<Object, Object>) JsonUtil.parseObject(json);
			if (jsonData.containsKey("data")) {
				Map<String, Object> data = (Map<String, Object>) jsonData.get("data");
				List<Map<Object, Object>> comments = (List<Map<Object, Object>>) data.get("comment");
				List<Map<String, Object>> rComments = new ArrayList<Map<String, Object>>();
				for (Map<Object, Object> comment : comments) {
					Map<String, Object> rComment = new HashMap<String, Object>();
					// COMMENT_TIME
					if(comment.containsKey("createTime")) {
					rComment.put(Constants.COMMENT_TIME, ConstantFunc.normalTime(comment.get("createTime").toString()));
					}
					// COMMENT_CONTENT
					if(comment.containsKey("content")) {
					rComment.put(Constants.COMMENT_CONTENT, comment.get("content"));
					}
					// UP_CNT
					if(comment.containsKey("upCount")) {
					rComment.put(Constants.UP_CNT, comment.get("upCount"));
					}
					// DOWN_CNT
					if(comment.containsKey("downCount")) {
					rComment.put(Constants.DOWN_CNT, comment.get("downCount"));
					}
					// COM_REPLY_CNT
					if(comment.containsKey("replyCount")) {
					rComment.put(Constants.COM_REPLY_CNT, comment.get("replyCount"));
					}
					// USERNAME
					Map<Object, Object> user = (Map<Object, Object>) comment.get("user");
					if (user.containsKey("userName")) {
						rComment.put(Constants.USERNAME, user.get("userName"));
					}
					rComments.add(rComment);
				}
				parsedata.put("comments", rComments);
			}

		} catch (Exception e) {
			LOG.error("json format conversion error in the executeParse() method", e);
		}
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
