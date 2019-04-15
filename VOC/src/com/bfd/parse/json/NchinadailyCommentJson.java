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

/**
 * @site 中国日报网(Nchinadaily)
 * @function 评论页
 *
 */
public class NchinadailyCommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NchinadailyCommentJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient normalizerClient, ParseUnit unit) {
		Map<String, Object> parsedata = new HashMap<String, Object>();
		JsonParserResult result = new JsonParserResult();
		try {
			String json = unit.getPageData();
			executeParse(parsedata, json, unit);
			result.setData(parsedata);
			result.setParsecode(0); // 成功
		} catch (Exception e) {
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
			result.setParsecode(5); // 失败
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata, String json, ParseUnit unit) {
		try {
			List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
			parsedata.put(Constants.TASKS, taskList); // tasks 字段是必须的
			Object obj = JsonUtil.parseObject(json);
			if (obj instanceof Map) {
				Map<String, Object> data = (Map<String, Object>) obj;
				// 评论部分
				if (data.containsKey("comments") && data.get("comments") instanceof List) {
					List<Map<String, Object>> comment = (List<Map<String, Object>>) data.get("comments");
					// 存放组装好的数据
					ArrayList<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
					for (int i = 0; i < comment.size(); i++) {
						// 临时存放数据的map
						HashMap<String, Object> tempmap = new HashMap<String, Object>();
						Map<String, Object> commMap = comment.get(i);
						// 评论人昵称
						if (commMap.containsKey("passport")) {
							Map<String, Object> replyUser = (Map<String, Object>) commMap.get("passport");
							if (replyUser.containsKey("nickname")) {
								String nickname = replyUser.get("nickname").toString();
								tempmap.put(Constants.USERNAME, nickname);
							}
						}
						// 评论时间
						if (commMap.containsKey("create_time")) {
							String postTime = commMap.get("create_time").toString();
							tempmap.put(Constants.COMMENT_TIME, postTime);
						}
						// 评论内容
						if (commMap.containsKey("content")) {
							String content = commMap.get("content").toString();
							tempmap.put(Constants.COMMENT_CONTENT, content);
						}
						tempList.add(tempmap);
					}
					parsedata.put(Constants.COMMENTS, tempList);

					/*
					 * 处理下一页，参考其他站点
					 * @see com.bfd.parse.reprocess.NsohutvCommentRe
					 */
					String url = unit.getUrl(); // &page_no=1&total_page_no=12
					int curPageNo = regMatchInt("page_no=(\\d+)", url);
					int totalNo = regMatchInt("total_page_no=(\\d+)", url);
					// 当前页大于0                                  下一页数字小于等于总页数
					if(curPageNo > 0 && ++curPageNo <= totalNo) {
						String nextPage = url.replace("&page_no=" + (curPageNo - 1), "&page_no=" + curPageNo);
						Map<String, Object> nextpageTask = new HashMap<String, Object>();
						nextpageTask.put(Constants.LINK, nextPage);
						nextpageTask.put(Constants.RAWLINK, nextPage);
						nextpageTask.put(Constants.LINKTYPE, "newscomment");
						taskList.add(nextpageTask);
						parsedata.put(Constants.NEXTPAGE, nextpageTask);
					}
				}
			}
		} catch (Exception e) {
			LOG.error("executeParse error " + unit.getUrl());
		}
	}
	
	private int regMatchInt(String reg, String mth) {
		int result = 0;
		Matcher match = Pattern.compile(reg).matcher(mth);
		if (match.find()) {
			return Integer.parseInt(match.group(1));
		}
		return result;
	}
}
