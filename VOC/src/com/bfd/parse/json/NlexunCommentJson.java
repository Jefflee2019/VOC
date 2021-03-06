package com.bfd.parse.json;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.TextUtil;
/**
 * 站点名：Lexun
 * <p>
 * 主要功能：获得评论相关信息，评论人，评论内容，评论时间
 * @author bfd_01
 *
 */
public class NlexunCommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NlexunCommentJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient normalizerClient,
			ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
		List<Map<String,Object>> taskList = new ArrayList<Map<String,Object>> ();
		parsedata.put("tasks", taskList);
		for (JsonData data : dataList) {
			if (!data.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(data, unit);
//			LOG.info("url:" + data.getUrl() + ".json is " + json);
			try {
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
						&& (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["),
							json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"),
							json.lastIndexOf("}") + 1);
				}
//				LOG.info("url:" + data.getUrl() + ".correct json is " + json);
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
//				e.printStackTrace();
//				LOG.warn("json :" + json + ".url:" + taskdata.get("url"));
				parsecode = 500012;
				LOG.warn(
						"AMJsonParser exception, taskdata url="
								+ taskdata.get("url") + ".jsonUrl :"
								+ data.getUrl(), e);
			}

		}
		JsonParserResult result = new JsonParserResult();
		try {
			result.setData(parsedata);
			result.setParsecode(parsecode);
		} catch (Exception e) {
//			e.printStackTrace();
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		try {
			List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
			parsedata.put(Constants.TASKS, taskList);
			Map<String, Object> map = (Map<String, Object>) JsonUtils
					.parseObject(json);
			int total = Integer.valueOf(map.get("total").toString());
			int pageCnt = total/10 +1;
			int pageNo = Integer.valueOf(map.get("page").toString());
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			parsedata.put(Constants.REPLY_CNT, total);
			if (map != null && map.containsKey("record")) {
				List<Map<String, Object>> comments = (List<Map<String, Object>>) map
						.get("record");
				for (int i = 0; i < comments.size(); i++) {
					Map<String, Object> comm = new HashMap<String, Object>();
					comm.put(Constants.UP_CNT, ((Map<String, Object>) comments
							.get(i)).get("Goodvotes"));
					comm.put(Constants.DOWN_CNT,
							((Map<String, Object>) comments.get(i))
									.get("Badvotes"));
					comm.put(Constants.COMMENT_CONTENT,
							((Map<String, Object>) comments.get(i))
									.get("Content"));
					comm.put(Constants.USERNAME,
							((Map<String, Object>) comments.get(i))
									.get("Rlynick"));
					comm.put(Constants.COMMENT_TIME, timeformat(comments.get(i)
							.get("Rlydate").toString()));
					list.add(comm);
				}
			}
			parsedata.put(Constants.COMMENTS, list);
			
			if (pageNo < pageCnt) {
				//http://sjnews.lexun.cn/touch/ajax/rlylist.aspx?topicid=173621&page=2&op=list
					String nextPage = url.replaceAll("page=" + pageNo, "page="
							+ (pageNo+1));
					Map<String, Object> nextpageTask = new HashMap<String, Object>();
					nextpageTask.put(Constants.LINK, nextPage);
					nextpageTask.put(Constants.RAWLINK, nextPage);
					nextpageTask.put(Constants.LINKTYPE, "newscomment");
					taskList.add(nextpageTask);
					parsedata.put(Constants.NEXTPAGE, nextpageTask);
					parsedata.put(Constants.TASKS, taskList);
			}
		} catch (Exception e) {
//			e.printStackTrace();
			LOG.error(e);
		}
	}

	private static String timeformat(String time) {
		// 04/14/2017 07:43:49
		String temp = time.substring(6, 10);
		time = temp + "/" + time.replace(time.substring(4, 9), "");
		SimpleDateFormat sdf = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" );
		Date date = new Date();
		try {
			date = sdf.parse(time);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return sdf.format(date);
	}
}
