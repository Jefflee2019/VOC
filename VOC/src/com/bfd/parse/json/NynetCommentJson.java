package com.bfd.parse.json;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;
/**
 * 站点名：北京青年报
 * 主要功能：获得评论相关信息，评论人，评论内容，评论时间
 * @author bfd_01
 *
 */
public class NynetCommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NynetCommentJson.class);
	@SuppressWarnings("unchecked")
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient normalizerClient,
			ParseUnit unit) {
		Map<String, Object> parsedata = new HashMap<String, Object>();
		List<Map<String,Object>> taskList = new ArrayList<Map<String,Object>> ();
		parsedata.put("tasks", taskList);
		int parsecode = 0;
		for (JsonData item : dataList) {
			if (!item.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(item, unit);
			Object obj = null;
			try {
				obj = JsonUtil.parseObject(json);
			} catch (Exception e) {
				LOG.error(e);
			}
			// 评论
			if (obj instanceof Map) {
				Map<String,Object> data = (Map<String,Object>) obj;
				Map<String,Object> temp = (Map<String,Object>) data.get("listData");
				List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
				int cmtsum = 0;
				if (temp != null && temp.containsKey("cmt_sum")) {
					cmtsum = (int) temp.get("cmt_sum");
				}
				parsedata.put(Constants.REPLY_CNT, cmtsum);
				
				if (temp != null && temp.containsKey("comments")) {
					List<Map<String,Object>> comments = (List<Map<String,Object>>) temp.get("comments");
					for (int i = 0; i < comments.size(); i++) {
						Map<String,Object> comm = new HashMap<String,Object>();
						comm.put(Constants.UP_CNT,
								((Map<String,Object>) comments.get(i)).get("support_count"));
						comm.put(Constants.DOWN_CNT,
								((Map<String,Object>) comments.get(i)).get("oppose_count"));
						comm.put(Constants.CITY,
								((Map<String,Object>) comments.get(i)).get("ip_location"));
						comm.put(Constants.COMMENTER_IP,
								((Map<String,Object>) comments.get(i)).get("ip"));
						comm.put(Constants.COMMENT_CONTENT,
								((Map<String,Object>) comments.get(i)).get("content"));
						comm.put(Constants.COMMENT_TIME, timeformat(Long
								.parseLong(((Map<String,Object>) comments.get(i)).get(
										"create_time").toString())));
						// 评论人名称
						Map<String,Object> passport = (Map<String,Object>) ((Map<String,Object>) comments.get(i))
								.get("passport");
						comm.put(Constants.USERNAME, passport.get("nickname")
								.toString());
						list.add(comm);
					}
				}
				parsedata.put(Constants.COMMENTS, list);
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
	
	private String timeformat(long time) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(time);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(gc.getTime());
	}
}
