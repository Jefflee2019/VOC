package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * @site 站长网-新闻(Nadmin5)
 * @function 新闻评论页 评论部分以及下一页问题
 * @author bfd_02
 *
 */

public class Nadmin5CommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(Nadmin5CommentJson.class);

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
				if (data.containsKey("listData")) {
					Map<String, Object> temp = (Map<String, Object>) data.get("listData");
					if (temp.containsKey("comments")) {
						List<Map<String, Object>> comments = (List<Map<String, Object>>) temp.get("comments");
						// 存放组装好的数据
						ArrayList<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
						for (Map<String, Object> commentsmap : comments) {
							// 临时存放数据的map
							HashMap<String, Object> tempmap = new HashMap<String, Object>();
							// 支持数
							Object upCnt = commentsmap.get("support_count");
							tempmap.put(Constants.UP_CNT, Integer.parseInt(upCnt.toString()));

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
							tempList.add(tempmap);
						}
						parsedata.put(Constants.COMMENTS, tempList);
					}

					// cal nextpage
					// Map<String, Object> count = (Map<String, Object>)
					// temp.get("count");
					// int totalpage =
					// Integer.parseInt(count.get("total").toString());
					// parsedata.put(Constants.REPLY_CNT, totalpage);
					// Matcher match = PAGEPATTERN.matcher(url);
					// if (match.find()) {
					// int currPage = Integer.parseInt(match.group(1));
					// int currPageSize = Integer.parseInt(match.group(2));
					// if (currPage < Math.ceil((double) totalpage /
					// currPageSize)) {
					// int page = currPage + 1;
					// String nextPage = url.replaceAll("page=" + currPage,
					// "page=" + page);
					// Map<String, Object> nextpageTask = new HashMap<String,
					// Object>();
					// nextpageTask.put(Constants.LINK, nextPage);
					// nextpageTask.put(Constants.RAWLINK, nextPage);
					// nextpageTask.put(Constants.LINKTYPE, "newscomment");
					// taskList.add(nextpageTask);
					// parsedata.put(Constants.NEXTPAGE, nextpageTask);
					// parsedata.put(Constants.TASKS, taskList);
					// }
					// }
				} else {
					LOG.warn("url:" + url + "do not have comment");
				}
			}
		} catch (Exception e) {
			LOG.error("executeParse error " + url);
		}
	}
}
