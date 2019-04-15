package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * @site 搜狐博客(Nsohublog)
 * @function 评论页以及下一页问题
 * @author bfd_02
 *
 */

public class NsohublogCommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NsohublogCommentJson.class);

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
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
						&& (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["),
							json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"),
							json.lastIndexOf("}") + 1);
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

				// 评论数
				if (data.containsKey("discusscount")) {
					int discusscount = Integer.parseInt(data.get("discusscount").toString());
					parsedata.put(Constants.REPLY_CNT, discusscount);
				}

				// 评论部分
				if (data.containsKey("discusss")) {
					List<Map<String, Object>> comment = (List<Map<String, Object>>) data.get("discusss");
					// 存放组装好的数据
					ArrayList<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
					for (int i = 0; i < comment.size(); i++) {
						// 临时存放数据的map
						HashMap<String, Object> tempmap = new HashMap<String, Object>();
						Map<String, Object> commMap = comment.get(i);
						// 评论人昵称
						if (commMap.containsKey("unick")) {
							String userName = commMap.get("unick").toString();
							tempmap.put(Constants.USERNAME, userName);
						}

						// 评论时间
						if (commMap.containsKey("createtime")) {
							String commentTime = commMap.get("createtime").toString();
							commentTime = ConstantFunc.normalTime(commentTime);
							
							tempmap.put(Constants.COMMENT_TIME, commentTime);
						}

						// 评论内容
						if (commMap.containsKey("content")) {
							String commentContent = commMap.get("content").toString();
							tempmap.put(Constants.COMMENT_CONTENT, commentContent);
						}
						tempList.add(tempmap);
					}
					parsedata.put(Constants.COMMENTS, tempList);
				}
			}
		} catch (Exception e) {
			LOG.error("executeParse error " + url);

		}
	}

}
