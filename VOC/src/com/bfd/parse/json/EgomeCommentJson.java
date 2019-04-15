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
 * @site：国美在线(Egome)
 * @function：评论页Json插件，处理评论
 * @author bfd_02
 *
 */

public class EgomeCommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(EgomeCommentJson.class);

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
				LOG.warn("AMJsonParser exception, taskdata url=" + taskdata.get("url") + ".jsonUrl :" + data.getUrl(),
						e);
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
		List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
		parsedata.put(Constants.TASKS, taskList);
		try {
			Object obj = JsonUtil.parseObject(json);
			if (obj instanceof Map) {
				Map<String, Object> data = (Map<String, Object>) obj;
				if (data.containsKey("totalCount")) {
					int totalCount = (int) data.get("totalCount");
					// 评论总数判断是否评论
					if (totalCount != 0 && data.containsKey("evaList")) {
						Map<String, Object> evaList = (Map<String, Object>) data.get("evaList");
						if (evaList.containsKey("Evalist")) {
							List<Map<String, Object>> Evalist = (List<Map<String, Object>>) evaList.get("Evalist");
							if (!Evalist.isEmpty()) {
								// 存放组装数据
								List<Map<String, Object>> commentList = new ArrayList<Map<String, Object>>();
								for (Map<String, Object> EvaMap : Evalist) {
									// 组装评论
									Map<String, Object> temMap = new HashMap<String, Object>();
									// 评论内容
									if (EvaMap.containsKey("appraiseElSum")) {
										String appraiseElSum = EvaMap.get("appraiseElSum").toString();
										temMap.put(Constants.COMMENT_CONTENT, appraiseElSum);
									}

									// 评论人
									if (EvaMap.containsKey("loginname")) {
										String loginname = EvaMap.get("loginname").toString();
										temMap.put(Constants.COMMENTER_NAME, loginname);
									} else {
										temMap.put(Constants.COMMENTER_NAME, "GomeUser");
									}

									// 评论时间
									if (EvaMap.containsKey("post_time")) {
										String postTime = EvaMap.get("post_time").toString();
										temMap.put(Constants.COMMENT_TIME, postTime);
									}

									// 评分星级
									if (EvaMap.containsKey("mscore")) {
										String mscore = EvaMap.get("mscore").toString();
										temMap.put(Constants.SCORE, Integer.parseInt(mscore));
									}

									// 评论人所在城市
									if (EvaMap.containsKey("receiveCity")) {
										String receiveCity = EvaMap.get("receiveCity").toString();
										temMap.put(Constants.CITY, receiveCity);
									}
									
									commentList.add(temMap);
								}
								parsedata.put(Constants.COMMENTS, commentList);
							}
						}
					}
					// 评论总数
					parsedata.put(Constants.REPLY_CNT, totalCount);

					// 好评数
					if (data.containsKey("good")) {
						int good = (int) data.get("good");
						parsedata.put(Constants.GOOD_CNT, good);
					}

					// 中评数
					if (data.containsKey("mid")) {
						int mid = (int) data.get("mid");
						parsedata.put(Constants.GENERAL_CNT, mid);
					}

					// 差评数
					if (data.containsKey("bad")) {
						int bad = (int) data.get("bad");
						parsedata.put(Constants.POOR_CNT, bad);
					}

					/**
					 * 拼接下一页链接
					 * http://ss.gome.com.cn/item/v1/prdevajsonp/appraiseNew/9134320732/1/all/0/10/flag/appraise
					 */
					getNextPage(parsedata, url, taskList, totalCount);
				}
			} else {
				LOG.warn("url:" + url + "do not have comment");
			}

		} catch (Exception e) {
			LOG.error("executeParse error " + url);
		}
	}

	private void getNextPage(Map<String, Object> parsedata, String url, List<Map<String, Object>> taskList,
			int totalCount) {
		Matcher match = Pattern.compile("appraiseNew/(\\S?\\d+)/(\\d+)/all").matcher(url);
		if (match.find()) {
			String itemid = match.group(1);
			int currPage = Integer.parseInt(match.group(2));
			if (currPage < Math.ceil((float) totalCount / 10)) {
				StringBuffer sb = new StringBuffer();
				String nextPage = sb.append("http://ss.gome.com.cn/item/v1/prdevajsonp/appraiseNew/")
						.append(itemid).append("/").append(currPage + 1).append("/all/1/492/flag/appraise/all?callback=all")
						.toString();

				Map<String, Object> nextpageTask = new HashMap<String, Object>();
				nextpageTask.put(Constants.LINK, nextPage);
				nextpageTask.put(Constants.RAWLINK, nextPage);
				nextpageTask.put(Constants.LINKTYPE, "eccomment");
				taskList.add(nextpageTask);
				parsedata.put(Constants.NEXTPAGE, nextpageTask);
				parsedata.put(Constants.TASKS, taskList);
			} else {
				parsedata.put(Constants.TASKS, taskList);
			}
		}
	}
}
