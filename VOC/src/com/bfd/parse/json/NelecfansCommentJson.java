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

/**
 * @ClassName: NelecfansCommentJson
 * @author: taihua.li
 * @date: 2019年3月25日 下午12:02:06
 * @Description:TODO(这里用一句话描述这个类的作用)
 */
public class NelecfansCommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NelecfansCommentJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient normalizerClient, ParseUnit unit) {
		int parsecode = 0;
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

	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata, String json, String url, ParseUnit unit) {
		try {
			Object obj = JsonUtil.parseObject(json);
			List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
			parsedata.put(Constants.TASKS, taskList);
			if (obj instanceof Map) {
				Map<String, Object> commentJson = (Map<String, Object>) obj;
				// 评论数
				if (commentJson.containsKey("count")) {
					int replyCnt = Integer.parseInt(commentJson.get("count").toString());
					parsedata.put(Constants.REPLY_CNT, replyCnt);

					// 下一页任务
					if (replyCnt > 10) {// 第一层简单过滤
						getNextpageTask(parsedata, url, taskList, replyCnt);
					}
				}
				// 评论内容
				if (commentJson.containsKey("data")) {
					List<Map<String, Object>> data = (List<Map<String, Object>>) commentJson.get("data");
					if (data != null && !data.isEmpty()) {
						getComments(parsedata, data);
					}
				}
			}
		} catch (Exception e) {
			LOG.error("executeParse error " + url);
		}
	}

	/**   
	 * @Title: getComments   
	 * @Description: TODO(这里用一句话描述这个方法的作用)   
	 * @param: @param parsedata
	 * @param: @param data      
	 * @return: void      
	 * @throws   
	 */ 
	private void getComments(Map<String, Object> parsedata, List<Map<String, Object>> data) {
		List<Map<String, Object>> commentList = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> commentMap : data) {
			Map<String, Object> commentTask = new HashMap<String, Object>();
			// 评论内容
			String msg = commentMap.get("msg").toString();
			commentTask.put(Constants.COMMENT_CONTENT, msg);

			// 评论时间
			String dtime = commentMap.get("dtime").toString();
			commentTask.put(Constants.COMMENT_TIME, dtime);

			// 评论人
			String username = commentMap.get("username").toString();
			commentTask.put(Constants.COMMENTER_NAME, username);

			commentList.add(commentTask);
		}
		parsedata.put(Constants.COMMENTS, commentList);
	}

	/**   
	 * @Title: getNextpageTask   
	 * @Description: TODO(这里用一句话描述这个方法的作用)   
	 * @param: @param parsedata
	 * @param: @param url
	 * @param: @param taskList
	 * @param: @param replyCnt      
	 * @return: void      
	 * @throws   
	 */ 
	private void getNextpageTask(Map<String, Object> parsedata, String url, List<Map<String, Object>> taskList,
			int replyCnt) {
		Matcher match = Pattern.compile("page=(\\d+)&").matcher(url);
		if (match.find()) {
			int pageno = Integer.parseInt(match.group(1));
			if ((double) replyCnt / 10 > pageno) {// 第二层过滤
				Map<String, Object> nextpageTask = new HashMap<String, Object>();
				String nextpage = url.replace("page=" + pageno, "page=" + (pageno + 1));
				nextpageTask.put(Constants.LINK, nextpage);
				nextpageTask.put(Constants.RAWLINK, nextpage);
				nextpageTask.put(Constants.LINKTYPE, "newscomment");
				taskList.add(nextpageTask);
				parsedata.put(Constants.NEXTPAGE, nextpageTask);
				parsedata.put(Constants.TASKS, taskList);
			}
		}
	}
}
