package com.bfd.parse.json;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

public class NsohutvCommentJson  implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NsohutvCommentJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient normalizerClient, ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
//		for (JsonData data : dataList) {
//			if (!data.downloadSuccess()) {
//				continue;
//			}
//			String json = TextUtil.getUnzipJson(data, unit);
//			try {
//				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0 && (json.indexOf("[") < json.indexOf("{"))) {
//					json = json.substring(json.indexOf("["), json.lastIndexOf("]") + 1);
//				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
//					json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
//				}
//				executeParse(parsedata, json, data.getUrl(), unit);
//			} catch (Exception e) {
//
//				LOG.warn("JsonParser exception, taskdata url=" + taskdata.get("url") + ".jsonUrl :" + data.getUrl(), e);
//			}
//		}
		JsonParserResult result = new JsonParserResult();
		try {
			String json = unit.getPageData();
			if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0 && (json.indexOf("[") < json.indexOf("{"))) {
				json = json.substring(json.indexOf("["), json.lastIndexOf("]") + 1);
			} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
				json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
			}
			executeParse(parsedata, json, unit.getUrl(), unit);
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
			if(obj != null && obj instanceof Map) { // url.contains("changyan.sohu.com")
				Map<String,Object> map = (Map<String,Object>) obj;
				//{"cmt_sum":2, "outer_cmt_sum": 2, "participation_sum": 8, "total_page_no": 1}
				List<Map<String, Object>> comments = (List<Map<String, Object>>) map.get("comments");
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				if(comments != null && comments.size() > 0) {
					ArrayList<Map<String, Object>> commList = new ArrayList<Map<String, Object>>();
					parsedata.put(Constants.COMMENTS, commList);
					Map<String, Object> tmpMap, commMap;
					for (int len = comments.size()-1; 0 <= len; len--) {
						commMap = new HashMap<>();
						tmpMap = comments.get(len);
						if(tmpMap.containsKey("content")) { // 评论内容
							commMap.put(Constants.COMMENT_CONTENT, tmpMap.get("content"));
						}
						if(tmpMap.containsKey("create_time")) { // 评论时间
							Long time = (Long) tmpMap.get("create_time");
							commMap.put(Constants.COMMENT_TIME, sdf.format(new Date(time)));
						}
						if(tmpMap.containsKey("passport")) { // 获取昵称
							Map<String, Object> passport = (Map<String,Object>) tmpMap.get("passport");
							commMap.put(Constants.USERNAME, passport.get("nickname"));
						}
						if(tmpMap.containsKey("ip_location")) {
							commMap.put(Constants.CITY, tmpMap.get("ip_location"));
						}
						if(tmpMap.containsKey("ip")) {
							commMap.put(Constants.COMMENTER_IP, tmpMap.get("ip"));
						}
						commList.add(commMap);
					}
					// &page_no=1&total_page_no=12
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
			LOG.error("json parse error or json is null", e);
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
