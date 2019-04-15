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
 * @site 凤凰网论坛(Bifeng)
 * @function 评论页以及下一页问题
 * @author bfd_02
 *
 */

public class BifengCommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(BifengCommentJson.class);
	private static final Pattern PAGEPATTERN = Pattern
			.compile("&pagesize=(\\d+)&p=(\\d+)");

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient normalizerClient,
			ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
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
				LOG.warn(
						"JsonParser exception, taskdata url="
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

	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		try {
			Object obj = JsonUtil.parseObject(json);
			List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
			parsedata.put(Constants.TASKS, taskList);
			if (obj instanceof Map) {
				Map<String, Object> data = (Map<String, Object>) obj;
				// 评论数
				int count = Integer.parseInt(data.get("count").toString());
				parsedata.put(Constants.REPLY_CNT, count);
				// 参与人数
				int joinCount = Integer.parseInt(data.get("join_count")
						.toString());
				parsedata.put(Constants.PARTAKE_CNT, joinCount);
				
				// deal comments
				if (data.containsKey("comments")) {
					List<Map<String, Object>> comments = (List<Map<String, Object>>) data
							.get("comments");
					// 用于存放组装数据
					List<Map<String, Object>> commentList = new ArrayList<Map<String, Object>>();
					// 用于存放临时数据的map

					for (Map<String, Object> comment : comments) {
						Map<String, Object> tempMap = new HashMap<String, Object>();
						// 评论人昵称
						Object username = comment.get("uname");
						tempMap.put(Constants.USERNAME, username.toString());
						// 评论人所在城市
						Object city = comment.get("ip_from");
						tempMap.put(Constants.CITY, city.toString());
						// 评论内容
						Object commentContent = comment
								.get("comment_contents");
						tempMap.put(Constants.COMMENT_CONTENT,
								commentContent.toString());
						// 推荐数
						Object upCnt = comment.get("uptimes");
						tempMap.put(Constants.UP_CNT,
								Integer.parseInt(String.valueOf(upCnt)));
						// 回复时间
						Object commentTime = comment.get("comment_date");
						tempMap.put(Constants.COMMENT_TIME,
								commentTime.toString());

						// deal 引用的评论信息refer_comments
						if (comment.containsKey("parent")) {
							List<Map<String, Object>> parentList = (List<Map<String, Object>>) comment
									.get("parent");
							// 最接近的一层引用
							if (parentList != null &&!parentList.isEmpty()) {
								Map<String, Object> parentMap = parentList
										.get(parentList.size() - 1);
								// 临时存放引用信息的map
								Map<String, Object> referMap = new HashMap<String, Object>();
								// 被引用人昵称
								Object referCommentUsername = parentMap
										.get("uname");
								referMap.put(Constants.REFER_COMM_USERNAME,
										referCommentUsername.toString());
								// 被引用人所在地
								Object referCommentCity = parentMap
										.get("ip_from");
								referMap.put(Constants.REFER_COMM_CITY,
										referCommentCity.toString());
								// 被引用评论内容
								Object referCommentContent = parentMap
										.get("comment_contents");
								referMap.put(Constants.REFER_COMM_CONTENT,
										referCommentContent.toString());
								// 被引用评论的支持数
								Object referUpCnt = parentMap.get("uptimes");
								referMap.put(Constants.REFER_UP_CNT, Integer
										.parseInt(String.valueOf(referUpCnt)));
								// 被引用评论时间
								Object referCommentTime = parentMap
										.get("comment_date");
								referMap.put(Constants.REFER_COMM_TIME,
										referCommentTime.toString());
								tempMap.put(Constants.REFER_COMMENTS,
										referMap);
							}
						}
						commentList.add(tempMap);
					}
					parsedata.put(Constants.COMMENTS, commentList);
				}

				// cal nextpage
				Matcher match = PAGEPATTERN.matcher(url);
				if (match.find()) {
					int currPageSize = Integer.parseInt(match.group(1));
					int currPage = Integer.parseInt(match.group(2));
					if (currPage < Math.ceil((double) count / currPageSize)) {
						int page = currPage + 1;
						String nextPage = url.replaceAll("p=" + currPage, "p="
								+ page);
						Map<String, Object> nextpageTask = new HashMap<String, Object>();
						nextpageTask.put(Constants.LINK, nextPage);
						nextpageTask.put(Constants.RAWLINK, nextPage);
						nextpageTask.put(Constants.LINKTYPE, "newscomment");
						taskList.add(nextpageTask);
						parsedata.put(Constants.NEXTPAGE, nextpageTask);
						parsedata.put(Constants.TASKS, taskList);
					}
				}
			}
		} catch (Exception e) {
			LOG.error("executeParse error " + url);
		}
	}
}
