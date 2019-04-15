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
 * @site 微客网-新闻(Nkn58)
 * @function 新闻评论页 评论部分以及下一页问题
 * @author bfd_02
 *
 */

public class Nkn58CommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(Nkn58CommentJson.class);

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
				//当前页评论数
				int pageCount = 0;
					if (data.containsKey("data")) {
						List<Map<String, Object>> dataList = (List<Map<String, Object>>) data.get("data");
						pageCount = dataList.size();
						// 存放组装好的数据
						ArrayList<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
						for (Map<String, Object> dataMap : dataList) {
							// 临时存放数据的map
							HashMap<String, Object> tempmap = new HashMap<String, Object>();
							// 评论人昵称
							Object username = dataMap.get("uname");
							tempmap.put(Constants.USER_NAME, username.toString());

							// 评论内容
							Object commentContent = dataMap.get("cnt");
							tempmap.put(Constants.COMMENT_CONTENT, commentContent.toString());

							// 评论时间
							Object commentTime = dataMap.get("time");
							commentTime = ConstantFunc.convertTime(commentTime.toString());
							tempmap.put(Constants.COMMENT_TIME, commentTime);

							// 评论回复数
							Object replyCnt = dataMap.get("rn");
							replyCnt = Integer.parseInt(replyCnt.toString());
							tempmap.put(Constants.COM_REPLY_CNT, replyCnt);

							// 支持数
							Object oldUpCnt = dataMap.get("prnum");
							int upCnt = Integer.parseInt(oldUpCnt.toString());
							tempmap.put(Constants.UP_CNT, upCnt);

							tempList.add(tempmap);
						}
						parsedata.put(Constants.COMMENTS, tempList);
					}

					// cal nextpage
					Pattern ptn = Pattern.compile("&pn=(\\d+)");
					Matcher match = ptn.matcher(url);
					if (match.find()) {
						//当前页码
						int currPage = Integer.parseInt(match.group(1));
						if (pageCount == 10) {
							int pn = currPage + 1;
							String nextPage = url.replaceAll("pn=" + currPage, "pn=" + pn);
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
		} catch (Exception e) {
			LOG.error("executeParse error " + url);
		}
	}
}
