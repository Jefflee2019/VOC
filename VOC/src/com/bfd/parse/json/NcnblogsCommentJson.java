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
 * 站点名：博客园
 * <p>
 * 主要功能：获得评论相关信息，评论人，评论内容，评论时间
 * @author bfd_01
 *
 */
public class NcnblogsCommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NcnblogsCommentJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient normalizerClient,
			ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
		List<Map<String,Object>> taskList = new ArrayList<Map<String,Object>> ();
		parsedata.put("tasks", taskList);
		for (JsonData data : dataList) {
			if (!data.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(data, unit);
//			LOG.info("url:" + data.getUrl() + ".json is " + json);
			try {
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
						&& (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["),
							json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"),
							json.lastIndexOf("}") + 1);
				}
//				LOG.info("url:" + data.getUrl() + ".correct json is " + json);
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
//				e.printStackTrace();
//				LOG.warn("json :" + json + ".url:" + taskdata.get("url"));
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
//			e.printStackTrace();
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		try {
			List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
			parsedata.put(Constants.TASKS, tasks);
			parseReplyHtml(json, parsedata);
		} catch (Exception e) {
			LOG.error(e);
		}
	}
	
	private void parseReplyHtml(String html, Map<String, Object> parseData){
		List<String> name = new ArrayList<String>();
		List<String> time = new ArrayList<String>();
		List<String> content = new ArrayList<String>();
		Pattern pname = Pattern.compile("class=\"comment-author\">(\\S+(\\s)?\\S+)</a>");
		Matcher mname = pname.matcher(html);
		Pattern ptime = Pattern.compile("<span class=\"time\">发表于 (\\d+-\\d+-\\d+ \\d+:\\d+) </span>");
		Matcher mtime = ptime.matcher(html);
		Pattern pcontent = Pattern.compile("class=\"comment_main\" id=\"\\S+\">(\\S+)</div>");
		Matcher mcontent = pcontent.matcher(html);
		while (mname.find()) {
			name.add(mname.group(1));
		}
		while (mtime.find()) {
			time.add(mtime.group(1));
		}
		while (mcontent.find()) {
			content.add(mcontent.group(1));
		}
		
		List<Map<String, Object>> comments = new ArrayList<Map<String, Object>>();
		if(!name.isEmpty() && name.size() == time.size() && time.size() == content.size()) {
			for (int i=0;i<name.size();i++) {
				Map<String, Object> comment = new HashMap<String, Object>();
				comment.put(Constants.COMMENTER_NAME, name.get(i).toString());
				comment.put(Constants.COMMENT_TIME, time.get(i).toString());
				comment.put(Constants.COMMENT_CONTENT, content.get(i).toString());
				comments.add(comment);
			}
		}
		parseData.put(Constants.COMMENTS, comments);
	}
}
