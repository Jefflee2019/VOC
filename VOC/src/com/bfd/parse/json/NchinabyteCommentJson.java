package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * 站点名：比特网
 * 
 * 功能：分离评论
 * 
 * @author bfd_06
 * 
 */
public class NchinabyteCommentJson implements JsonParser {

	private static final Log LOG = LogFactory
			.getLog(NchinabyteCommentJson.class);

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
			LOG.error("JsonParse reprocess error, taskdat url=" + taskdata.get("url"), e);
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		List<Map<String, Object>> rtasks = new ArrayList<Map<String, Object>>();
		parsedata.put(Constants.TASKS, rtasks);
		try {
			Map result = (Map) JsonUtil.parseObject(json);
			Object parentPosts = result.get("parentPosts");
			if (parentPosts instanceof Map) {
				Map comments = (Map) parentPosts;
				Set keySet = comments.keySet();
				List<Object> newComments = new ArrayList<Object>();
				for (Object key : keySet) {
					Map comment = (Map) comments.get(key);
					Map<Object, Object> newComment = new HashMap<Object, Object>();
					newComment.put(Constants.COMMENT_CONTENT,
							comment.get("message"));
					String commentTime = (String) comment.get("created_at");
					commentTime = commentTime.substring(0,
							commentTime.indexOf('T'));
					newComment.put(Constants.COMMENT_TIME, commentTime);
					newComment.put(Constants.UP_CNT, comment.get("likes"));
					Map author = (Map) comment.get("author");
					newComment.put(Constants.USERNAME, author.get("name"));
					newComments.add(newComment);
				}
				parsedata.put("comments", newComments);
				Map<String, Object> cursor = (Map<String, Object>) result
						.get("cursor");
				int totalPageNum = (int) cursor.get("pages");
				String currPageNum = match("page=(\\d+)", unit.getUrl());
				if (currPageNum != null
						&& Integer.parseInt(currPageNum) != totalPageNum) {
					Map<String, Object> rtask = new HashMap<String, Object>();
					String nextUrl = unit.getUrl();
					nextUrl = nextUrl.replace("page=" + currPageNum, "page="
							+ (Integer.parseInt(currPageNum) + 1));
					rtask.put("link", nextUrl);
					rtask.put("rawlink", nextUrl);
					rtask.put("linktype", "newscomment");
					rtasks.add(rtask);
					parsedata.put(Constants.NEXTPAGE, rtask);
				}
				
				
			}
		} catch (Exception e) {
			LOG.error(
					"json format conversion error in the executeParse() method",
					e);
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
