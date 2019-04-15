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
 * @site 安卓网-新闻(Nhiapk)
 * @function 新闻评论页 评论部分以及下一页问题
 * @author bfd_02
 *
 */

public class NhiapkCommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NhiapkCommentJson.class);

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
				Map<String, Object> data = (Map<String, Object>) obj;
				String topicId = null;
				int cmtSum = 0;
				List<Map<String, Object>> comments = null;
				if (data.containsKey("listData")) {
					Map<String, Object> temp = (Map<String, Object>) data.get("listData");
					topicId = temp.get("topic_id").toString();
					cmtSum = Integer.parseInt(temp.get("cmt_sum").toString());
					comments = (List<Map<String, Object>>) temp.get("comments");
				} else if (data.containsKey("comments")) {
					comments = (List<Map<String, Object>>) data.get("comments");
					topicId = data.get("topic_id").toString();
					cmtSum = Integer.parseInt(data.get("cmt_sum").toString());
				} else {
					LOG.warn("url:" + url + "do not have comment");
				}
				// 存放组装好的数据
				ArrayList<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
				for (Map<String, Object> commentsmap : comments) {
					// 临时存放数据的map
					HashMap<String, Object> tempmap = new HashMap<String, Object>();
					// 支持数
					Object upCnt = commentsmap.get("support_count");
					tempmap.put(Constants.UP_CNT, Integer.parseInt(upCnt.toString()));
					// 反对数
					Object downCnt = commentsmap.get("oppose_count");
					tempmap.put(Constants.DOWN_CNT, Integer.parseInt(downCnt.toString()));

					// 回复数
					Object replyCount = commentsmap.get("reply_count");
					tempmap.put(Constants.REPLY_CNT, Integer.parseInt(replyCount.toString()));

					// 评论内容
					Object content = commentsmap.get("content");
					tempmap.put(Constants.COMMENT_CONTENT, content.toString());

					// 评论时间
					Object createTime = commentsmap.get("create_time");
					String commentTime = ConstantFunc.transferLongToDate("yyyy/MM/dd HH:mm",
							Long.valueOf(createTime.toString()));
					tempmap.put(Constants.COMMENT_TIME, commentTime);

					// 评论人昵称
					if (commentsmap.containsKey("passport")) {
						Map<String, Object> passport = (Map<String, Object>) commentsmap.get("passport");
						if (passport.containsKey("nickname")) {
							String nickname = passport.get("nickname").toString();
							tempmap.put(Constants.COMMENTER_NAME, nickname);
						}
					}

					// 获取评论的引用
					if (commentsmap.containsKey("comments")) {
						List<Map<String, Object>> referCommentsList = (List<Map<String, Object>>) commentsmap
								.get("comments");
						if (referCommentsList != null && !referCommentsList.isEmpty()) {
							Map<String, Object> referCommentsDataMap = new HashMap<String, Object>();
							Map<String, Object> referCommentsMap = (Map<String, Object>) referCommentsList
									.get(referCommentsList.size() - 1);
							referCommentsDataMap.put(Constants.REFER_COMM_USERNAME,
									((HashMap<String, Object>) referCommentsMap.get("passport")).get("nickname"));
							referCommentsDataMap.put(Constants.REFER_COMM_CONTENT, referCommentsMap.get("content"));
							referCommentsDataMap.put(Constants.REFER_UP_CNT, referCommentsMap.get("support_count"));
							referCommentsDataMap.put(Constants.REFER_DOWN_CNT, referCommentsMap.get("oppose_count"));

							tempmap.put(Constants.REFER_COMMENTS, referCommentsDataMap);
						}
					}
					tempList.add(tempmap);
				}
				parsedata.put(Constants.COMMENTS, tempList);

				// cal nextpage
				String nextPage = null;
				String sClientId = "";
				int pageNo = 0;
				int pageSize = 0;

				Pattern pattern = Pattern
						.compile(
								"http://changyan.sohu.com/api/2/topic/comments\\?client_id=(\\w+)&topic_id=(\\d+)&page_size=(\\d+)&page_no=(\\d+)",
								Pattern.DOTALL);
				Matcher matcher = pattern.matcher(url);

				if (matcher.find()) {
					sClientId = matcher.group(1);
					topicId = matcher.group(2);
					pageSize = Integer.parseInt(matcher.group(3));
					pageNo = Integer.parseInt(matcher.group(4));
				} else {
					pattern = Pattern.compile("http://changyan.sohu.com/node/html\\?client_id=(\\w+)");
					matcher = pattern.matcher(url);
					if (matcher.find()) {
						sClientId = matcher.group(1);
					}
				}
				if (url.contains("&page_no")) {
					if (pageNo < Math.ceil((double) cmtSum / pageSize)) {
						nextPage = "http://changyan.sohu.com/api/2/topic/comments\\?client_id=" + sClientId
								+ "&topic_id=" + topicId + "&page_size=" + pageSize + "&page_no=" + (pageNo + 1);
					}
				} else {
					if (Math.ceil((double) cmtSum / pageSize) > 1) {
						nextPage = "http://changyan.sohu.com/api/2/topic/comments\\?client_id=" + sClientId
								+ "&topic_id=" + topicId + "&page_size=" + 6 + "&page_no=" + 2;
					}
				}

				Map<String, Object> nextpageTask = new HashMap<String, Object>();
				nextpageTask.put(Constants.LINK, nextPage);
				nextpageTask.put(Constants.RAWLINK, nextPage);
				nextpageTask.put(Constants.LINKTYPE, "newscomment");
				taskList.add(nextpageTask);
				parsedata.put(Constants.NEXTPAGE, nextpageTask);
				parsedata.put(Constants.TASKS, taskList);
			}
		} catch (Exception e) {
			LOG.error("executeParse error " + url);
		}
	}

}
