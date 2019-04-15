package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.TextUtil;
/**
 * 站点名：博客园
 * <p>
 * 主要功能：获得评论相关信息，评论人，评论内容，评论时间
 * @author bfd_01
 *
 */
public class Nigao7CommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(Nigao7CommentJson.class);

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
			Map<String,Object> map = (Map<String, Object>) JsonUtils.parseObject(json);
			
			Map<String,Object> allcount = (Map<String,Object>)map.get("allCount");
			int total = Integer.valueOf(allcount.get("num").toString());
			
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			parsedata.put(Constants.REPLY_CNT, total);
			if (map != null && map.containsKey("comment")) {
				Map comments = (Map) map.get("comment");
				Iterator<Map.Entry<Integer, Map>> it = comments.entrySet().iterator();
				while (it.hasNext()) {
					Map<String, Object> comm = new HashMap<String, Object>();
					Map.Entry<Integer, Map> entry = it.next();
					Map temp = (Map) entry.getValue();
					comm.put(Constants.USERNAME, temp.get("nickname"));
					comm.put(Constants.COMMENT_CONTENT, temp.get("content"));
					comm.put(Constants.COMMENT_TIME, ConstantFunc
							.normalTime(temp.get("origin_created").toString()));
					list.add(comm);
				}
			}
			parsedata.put(Constants.COMMENTS, list);
		} catch (Exception e) {
			LOG.error(e);
		}
	}	
}
