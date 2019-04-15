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

public class NhuanqiuCommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NhuanqiuCommentJson.class);

	private static final Pattern PAGEPATTERN = Pattern.compile("&n=(\\d+)&p=(\\d+)&");

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
				if ((json.indexOf("[") >= 0) && (json.indexOf("]") >= 0) && (json.indexOf("[") < json.indexOf("{")))
					json = json.substring(json.indexOf("["), json.lastIndexOf("]") + 1);
				else if ((json.indexOf("{") >= 0) && (json.indexOf("}") > 0)) {
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
			parsedata.put("tasks", taskList);
			if ((obj instanceof Map)) {
				Map<String, Object> jsonData = (Map<String, Object>) obj;
				if (jsonData.containsKey("data")) {
					List<Map<String, Object>> data = (List<Map<String, Object>>) jsonData.get("data");

					ArrayList<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
					for (Map<String, Object> comment : data) {
						HashMap<String, Object> tempmap = new HashMap<String, Object>();

						Object city = comment.get("loc");
						tempmap.put(Constants.CITY, city.toString());

						Object commentContent = comment.get("content");
						tempmap.put(Constants.COMMENT_CONTENT, commentContent.toString());

						Object commentTime = comment.get("ctime");
						commentTime = ConstantFunc.normalTime(commentTime.toString());
						tempmap.put(Constants.COMMENT_TIME, commentTime);

						Object oldUpCnt = comment.get("n_active");
						int upCnt = Integer.parseInt(oldUpCnt.toString());
						tempmap.put(Constants.UP_CNT, Integer.valueOf(upCnt));

						Object oldComReplyCnt = comment.get("n_reply");
						int ComReplyCnt = Integer.parseInt(oldComReplyCnt.toString());
						tempmap.put(Constants.COM_REPLY_CNT, Integer.valueOf(ComReplyCnt));

						if (comment.containsKey("user")) {
							Object user = comment.get("user");
							if ((user instanceof Map)) {
								Map<String, Object> userMap = (Map<String, Object>) user;
								if (userMap.containsKey("nickname")) {
									String nickname = userMap.get("nickname").toString();
									tempmap.put(Constants.REFER_COMM_USERNAME, nickname);
								}
							}

						}

						if (comment.containsKey("reply")) {
							Object reply = comment.get("reply");
							if ((reply instanceof List)) {
								List<Map<String, Object>> replyList = (List<Map<String, Object>>) reply;

								List<Map<String, Object>> refers = new ArrayList<Map<String, Object>>();
								if ((replyList != null) && (!replyList.isEmpty())) {
									Map<String, Object> firstMap = (Map<String, Object>) replyList.get(0);

									Map<String, Object> tempReplyMap = new HashMap<String, Object>();

									if (firstMap.containsKey("content")) {
										String referCommContent = firstMap.get("content").toString();
										tempReplyMap.put(Constants.REFER_COMM_CONTENT, referCommContent);

										if (firstMap.containsKey("loc")) {
											String referCommCity = firstMap.get("loc").toString();
											tempReplyMap.put(Constants.REFER_COMM_CITY, referCommCity);
										}

										if (firstMap.containsKey("n_active")) {
											String referUpCnt = firstMap.get("n_active").toString();
											tempReplyMap.put(Constants.REFER_UP_CNT, referUpCnt);
										}

										if (firstMap.containsKey("ctime")) {
											String referCommTime = firstMap.get("ctime").toString();
											referCommTime = ConstantFunc.normalTime(referCommTime);
											tempReplyMap.put(Constants.REFER_COMM_TIME, referCommTime);
										}

										if (firstMap.containsKey("user")) {
											Object user = firstMap.get("user");
											if ((user instanceof Map)) {
												Map<String, Object> userMap = (Map<String, Object>) user;
												if (userMap.containsKey("nickname")) {
													String nickname = userMap.get("nickname").toString();
													tempReplyMap.put(Constants.REFER_COMM_USERNAME, nickname);
												}
											}
										}
										refers.add(tempReplyMap);
									}
								}
								tempmap.put(Constants.REFER_COMMENTS, refers);
							}
						}
						tempList.add(tempmap);
						parsedata.put(Constants.COMMENTS, tempList);
					}

					int totalpage = data.size();
					Matcher match = PAGEPATTERN.matcher(url);
					if (match.find()) {
						int currPage = Integer.parseInt(match.group(1));
						int currPageSize = Integer.parseInt(match.group(2));

						if (totalpage == currPageSize) {
							int page = currPage + 1;
							String nextPage = url.replaceAll("page=" + currPage, "page=" + page);
							Map<String, Object> nextpageTask = new HashMap<String, Object>();
							nextpageTask.put(Constants.LINK, nextPage);
							nextpageTask.put(Constants.RAWLINK, nextPage);
							nextpageTask.put(Constants.LINKTYPE, "newscomment");
							taskList.add(nextpageTask);
							parsedata.put(Constants.NEXTPAGE, nextpageTask);
							parsedata.put(Constants.TASKS, taskList);
						}
					}
				} else {
					LOG.warn("url:" + url + "do not have comment");
				}
			}
		} catch (Exception e) {
			LOG.error("executeParse error " + url);
		}
	}
}