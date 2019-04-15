package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * @site 和讯博客(Nhexunblog)
 * @function 评论页以及下一页问题
 * @author bfd_02
 *
 */

public class NhexunblogCommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NhexunblogCommentJson.class);

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
				if (json.indexOf("(") >= 0 && json.indexOf(")") > 0) {
					json = json.substring(json.indexOf("(") + 1, json.lastIndexOf(")"));
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
				// 评论部分

				if (data.containsKey("comment") && data.get("comment") instanceof List) {
					List<Map<String, Object>> comment = (List<Map<String, Object>>) data.get("comment");
					// 存放组装好的数据
					ArrayList<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
					for (int i = 0; i < comment.size(); i++) {
						// 临时存放数据的map
						HashMap<String, Object> tempmap = new HashMap<String, Object>();
						Map<String, Object> commMap = comment.get(i);
						// 评论人昵称
						if (commMap.containsKey("UserNameNewTemp")) {
							String userNameNewTemp = commMap.get("UserNameNewTemp").toString();
							String regex = ">(\\S*)</a>";
							regMatch(regex, userNameNewTemp);
							tempmap.put(Constants.USERNAME, regMatch(regex, userNameNewTemp));
						}

						
						// 评论时间
						if (commMap.containsKey("PostTime")) {
							String postTime = commMap.get("PostTime").toString();
							tempmap.put(Constants.COMMENT_TIME, postTime);
						}

						// 评论内容
						if (commMap.containsKey("Content")) {
							String content = commMap.get("Content").toString();
							// 如果有多层引用,取最后一个</div></div>之后的内容
							String flag = "</div></div>";
							String regex = "</div></div>(\\S*)";
							String con = splitMatch(content, flag, regex);
							if (!con.equals("")) {
								con = con.replaceAll("(<img\\s*src=\\S*\\s*border=\\S*\\s*alt=\\S*\\s*/>)", "");
							}
							tempmap.put(Constants.COMMENT_CONTENT, con);
						}

						// 引用部分，包括被引用评论时间、内容、被引用人昵称
						// 临时存放引用信息的map
						if (commMap.containsKey("Content") && commMap.get("Content").toString().contains("quotefrom")) {
							Map<String, Object> referMap = new HashMap<String, Object>();
							String content = commMap.get("Content").toString();

							// 时间 匹配第一个
							String timeRex = "(\\d+-\\d+-\\d+\\s*\\d+:\\d+?(:\\d+))";
							regMatch(timeRex, content);
							referMap.put(Constants.REFER_COMM_TIME, regMatch(timeRex, content));

							// 取出无关干扰内容
							if (content.contains("<div class='ReplyTable_B_2'>")) {
								content = content.replaceAll("<div\\s*class=\'ReplyTable_B_2\'>", "");
							}

							// 内容，如果多个，匹配倒数第二个</div></div>之后的内容，即被引用内容
							String flag = "</div></div>";
							String contRex = "'quotecontent'>([\\s\\S]*)</div></div>";
							String referCon = splitMatch(content, flag, contRex);
							if (!referCon.equals("")) {
								referCon = referCon.replaceAll("(<img\\s*src=\\S*\\s*border=\\S*\\s*alt=\\S*\\s*/>)", "");
								referMap.put(Constants.REFER_COMM_CONTENT, referCon);
							}

							// 昵称，匹配第一个
							String authRex = "以下是引用\\s*(\\S*)\\s*于";
							regMatch(authRex, content);
							referMap.put(Constants.REFER_COMM_USERNAME, regMatch(authRex, content));

							tempmap.put(Constants.REFER_COMMENTS, referMap);
						}
						tempList.add(tempmap);
					}
					parsedata.put(Constants.COMMENTS, tempList);

					// 处理下一页
					String nextReg = "&page=(\\d+)";
					int pageid = Integer.parseInt(regMatch(nextReg, url));
					// 评论数
					int count = comment.size();
					if (count == 10) {
						String nextPage = url.replace("&page=" + pageid, "&page=" + (pageid + 1));
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

	private String splitMatch(String content, String flag, String regex) {
		int count = StringUtils.countMatches(content, flag);
		String comCont = "";
		// 引用了多层评论
		if (count > 1) {
			String[] conStr = content.split(flag);
			// 如果是引用的内容
			if (regex.contains("quotecontent")) {
				comCont = conStr[conStr.length - 2];
				return comCont;
			} else {// 当前评论内容
				comCont = conStr[conStr.length - 1];
				return comCont;
			}
			// 只引用了一层评论
		} else if (count == 1 && regex.contains("quotecontent")) {
			return regMatch(regex, content);
		} else if (count == 1 && !regex.contains("quotecontent")) {
			comCont = regMatch(regex, content);
			return comCont;
		} else {
			return content;
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
