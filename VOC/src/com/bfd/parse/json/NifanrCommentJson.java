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
 * 站点名：爱范儿(新闻)
 * 
 * 主要功能：获取评论信息
 * 
 * @author bfd_03
 *
 */
public class NifanrCommentJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(NifanrCommentJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient urlnormalizerClients, ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();

		// 遍历dataList
		for (Object obj : dataList) {
			JsonData data = (JsonData) obj;
			// 判断该ajax数据是否下载成功
			if (!data.downloadSuccess()) {
				continue;
			}
			// 解压缩ajax数据
			String json = TextUtil.getUnzipJson(data, unit);
			try {
				// 将ajax数据转化为json数据格式
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0 && (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["), json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
				}
				// 执行从json数据中提取自己感兴趣的数据
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
				// e.printStackTrace();
				// LOG.warn("json:" + json + ".url:" + taskdata.get("url"));
				LOG.warn("AMJsonParse exception,taskdat url=" + taskdata.get("url") + ".jsonUrl:" + data.getUrl(), e);
			}
		}

		// 组装返回结果
		JsonParserResult result = new JsonParserResult();
		try {
			result.setParsecode(parsecode);
			result.setData(parsedata);
		} catch (Exception e) {
			// e.printStackTrace();
			LOG.error("jsonparser error url:" + taskdata.get("url"));
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void executeParse(Map<String, Object> parsedata, String json, String url, ParseUnit unit) {
		List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
		parsedata.put(Constants.TASKS, taskList);
		Object obj = null;
		try {
			obj = JsonUtil.parseObject(json);
		} catch (Exception e) {
			LOG.error("the data is null or fails to convert to json");
		}
		if (obj instanceof Map) {
			Map objMap = (HashMap) obj;
			//评论总数
			if(objMap.containsKey("meta")) {
				Map<String,Object> metaMap = (Map<String, Object>) objMap.get("meta");
				if(metaMap.containsKey("total_count")) {
					int totalCnt = Integer.parseInt(metaMap.get("total_count").toString());
					parsedata.put(Constants.REPLY_CNT, totalCnt);
				}
			}
			
			// 详情页评论信息
			List comments = null;
			if (objMap.containsKey("objects")) {
				comments = (ArrayList) objMap.get("objects");
			} else {
				return;
			}
			List commentsDataList = new ArrayList();
			Map commentsDataMap = null;
			for (Object tempObj : comments) {
				Map commentsMap = (HashMap) tempObj;
				commentsDataMap = new HashMap();

				// 获取评论时间
				if (commentsMap.containsKey("created_at")) {
					String sCreateTime = ConstantFunc.normalTime(commentsMap.get("created_at").toString());
					commentsDataMap.put(Constants.COMMENT_TIME, sCreateTime);
				}
				// 评论人姓名
				if (commentsMap.containsKey("author_name")) {
					String authorName = commentsMap.get("author_name").toString();
					commentsDataMap.put(Constants.COMMENTER_NAME, authorName);
				}
				// 评论时间
				if (commentsMap.containsKey("content")) {
					String sCommentContent = commentsMap.get("content").toString();
					commentsDataMap.put(Constants.COMMENT_CONTENT, sCommentContent);
				}
				// 评论点赞数
				if (commentsMap.containsKey("upvotes")) {
					int upCnt = Integer.parseInt(commentsMap.get("upvotes").toString());
					commentsDataMap.put(Constants.UP_CNT, upCnt);
				}
				// 评论反对数
				if (commentsMap.containsKey("downvotes")) {
					int downCnt = Integer.parseInt(commentsMap.get("downvotes").toString());
					commentsDataMap.put(Constants.DOWN_CNT, downCnt);
				}

				commentsDataList.add(commentsDataMap);
			}

			parsedata.put(Constants.COMMENTS, commentsDataList);

		}

	}

}
