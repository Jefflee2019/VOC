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
 * @site 新浪网-新闻(Nsina)
 * @function 新闻评论页 评论部分以及下一页问题
 * @author bfd_02
 *
 */

public class NsinaCommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NsinaCommentJson.class);
	private static final Pattern PAGEPATTERN = Pattern.compile("page=(\\d+)&page_size=(\\d+)");

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
				if (data.containsKey("result")) {
					Map<String, Object> temp = (Map<String, Object>) data.get("result");
					if (temp.containsKey("cmntlist")) {
						List<Map<String, Object>> cmntlist = (List<Map<String, Object>>) temp.get("cmntlist");
						// 存放组装好的数据
						ArrayList<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
						for (Map<String, Object> cmntlistmap : cmntlist) {
							// 临时存放数据的map
							HashMap<String, Object> tempmap = new HashMap<String, Object>();
							// 评论人昵称
							Object username = cmntlistmap.get("nick");
							tempmap.put(Constants.USER_NAME, username.toString());

							// 评论内容
							Object commentContent = cmntlistmap.get("content");
							tempmap.put(Constants.COMMENT_CONTENT, commentContent.toString());

							// 评论时间
							Object commentTime = cmntlistmap.get("time");
							tempmap.put(Constants.COMMENT_TIME, commentTime.toString());

							// 评论人所在城市
							Object city = cmntlistmap.get("area");
							tempmap.put(Constants.CITY, city.toString());

							// 支持数
							Object oldUpCnt = cmntlistmap.get("agree");
							int upCnt = Integer.parseInt(oldUpCnt.toString());
							tempmap.put(Constants.UP_CNT, upCnt);

							tempList.add(tempmap);
						}
						parsedata.put(Constants.COMMENTS, tempList);
					}

					// cal nextpage
					Map<String, Object> count = (Map<String, Object>) temp.get("count");
					//参与人数
					int partakeCnt = Integer.parseInt(count.get("total").toString());
					parsedata.put(Constants.PARTAKE_CNT, partakeCnt);
					//评论总数
					int replyCnt = Integer.parseInt(count.get("show").toString());
					parsedata.put(Constants.REPLY_CNT, replyCnt);
					
					Matcher match = PAGEPATTERN.matcher(url);
					if (match.find()) {
						int currPage = Integer.parseInt(match.group(1));
						int currPageSize = Integer.parseInt(match.group(2));
						if (currPage < Math.ceil((double)replyCnt / currPageSize)) {
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
