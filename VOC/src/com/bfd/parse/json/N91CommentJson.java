package com.bfd.parse.json;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
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
 * 站点名：91门户
 * <p>
 * 主要功能：获得评论相关信息，评论人，评论内容，评论时间
 * @author bfd_01
 *
 */
public class N91CommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(N91CommentJson.class);

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
			int total = Integer.valueOf(map.get("cmt_sum").toString());
//			int pageCnt = total/10 +1;
//			int pageNo = Integer.valueOf(map.get("page").toString());
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			parsedata.put(Constants.REPLY_CNT, total);
			if (map != null && map.containsKey("comments")) {
				List<Map<String, Object>> comments = (List<Map<String, Object>>) map
						.get("comments");
				for (int i = 0; i < comments.size(); i++) {
					Map<String, Object> comm = new HashMap<String, Object>();
					comm.put(Constants.UP_CNT, ((Map<String, Object>) comments
							.get(i)).get("support_count"));
					comm.put(Constants.COMMENT_CONTENT,
							((Map<String, Object>) comments.get(i))
									.get("content"));
					Map userinfo = (Map<String, Object>) comments.get(i)
							.get("metadataAsJson");
					comm.put(Constants.USERNAME,
							((Map<String, Object>) userinfo)
									.get("author"));
					comm.put(Constants.COMMENT_TIME, timeformat(Long.parseLong(comments.get(i)
							.get("create_time").toString())));
					list.add(comm);
				}
			}
			parsedata.put(Constants.COMMENTS, list);
			
//			if (pageNo < pageCnt) {
//				//http://sjnews.lexun.cn/touch/ajax/rlylist.aspx?topicid=173621&page=2&op=list
//					String nextPage = url.replaceAll("page=" + pageNo, "page="
//							+ (pageNo+1));
//					Map<String, Object> nextpageTask = new HashMap<String, Object>();
//					nextpageTask.put(Constants.LINK, nextPage);
//					nextpageTask.put(Constants.RAWLINK, nextPage);
//					nextpageTask.put(Constants.LINKTYPE, "newscomment");
//					taskList.add(nextpageTask);
//					parsedata.put(Constants.NEXTPAGE, nextpageTask);
//					parsedata.put(Constants.TASKS, taskList);
//			}
		} catch (Exception e) {
//			e.printStackTrace();
			LOG.error(e);
		}
	}

	private String timeformat(long time) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(time);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(gc.getTime());
	}
}
