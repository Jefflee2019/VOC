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
 * @site 东方财富网博客(Neastmoneyblog)
 * @function 评论页以及下一页问题
 * @author bfd_02
 *
 */

public class NeastmoneyblogCommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NeastmoneyblogCommentJson.class);

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
				// 评论总数
				double count = 0;
				if (data.containsKey("count")) {
					count = Float.parseFloat((data.get("count").toString()));
				}
				// 评论部分
				if (data.containsKey("re") && data.get("re") instanceof List) {
					List<Map<String, Object>> comment = (List<Map<String, Object>>) data.get("re");
					// 存放组装好的数据
					ArrayList<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
					for (int i = 0; i < comment.size(); i++) {
						// 临时存放数据的map
						HashMap<String, Object> tempmap = new HashMap<String, Object>();
						Map<String, Object> commMap = comment.get(i);
						// 评论人昵称
						if (commMap.containsKey("reply_user")) {
							Map<String, Object> replyUser = (Map<String, Object>) commMap.get("reply_user");
							String replyIp = commMap.get("reply_ip").toString();
							if (replyUser.containsKey("user_nickname")) {
								String nickname = replyUser.get("user_nickname").toString();
								if(!nickname.equals("")) {
								tempmap.put(Constants.COMMENTER_NAME, nickname);
								}else if(nickname.equals("")&&!replyIp.equals("")) {
									tempmap.put(Constants.COMMENTER_NAME, replyIp);	
								}else {
									tempmap.put(Constants.COMMENTER_NAME, "");
								}
							}
						}

						// 评论时间
						if (commMap.containsKey("reply_publish_time")) {
							String postTime = commMap.get("reply_publish_time").toString();
							tempmap.put(Constants.COMMENT_TIME, postTime);
						}

						// 评论内容
						if (commMap.containsKey("reply_text")) {
							String content = commMap.get("reply_text").toString();
							// 如果有多层引用,取最后一个</div></div>之后的内容
							tempmap.put(Constants.COMMENT_CONTENT, content);
						}
						tempList.add(tempmap);
					}
					parsedata.put(Constants.COMMENTS, tempList);

					// 处理下一页
					String nextReg = "&p=(\\d+)&";
					String sizeReg = "&ps=(\\d+)&";
					// 当前页码
					int pageid = Integer.parseInt(regMatch(nextReg, url));
					// 每页评论数
					double pagesize = Float.parseFloat(regMatch(sizeReg, url));
					// 判断翻页 = ？评论数/每页评论数：页码
					if (count / pagesize > pageid) {
						
						String nextPage = url.replace("&p=" + pageid, "&p=" + (pageid + 1));
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

	private String regMatch(String reg, String mth) {
		String result = "0";
		Matcher match = Pattern.compile(reg).matcher(mth);
		if (match.find()) {
			return match.group(1);
		}
		return result;
	}
}
