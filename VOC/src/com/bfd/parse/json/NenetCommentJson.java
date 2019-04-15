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

public class NenetCommentJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(NenetCommentJson.class);
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient normalizerClient,
			ParseUnit unit) {
		Map<String, Object> parsedata = new HashMap<String, Object>();
		List<Map<String,Object>> taskList = new ArrayList<Map<String,Object>> ();
		parsedata.put("tasks", taskList);
		int parsecode = 0;
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
				LOG.warn("url:" + taskdata.get("url"));
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
			if (obj instanceof List) {
				//存放组装数据的集合
				List<Map<String,Object>> commentList = new ArrayList<Map<String,Object>>();
				List<Map<String,Object>> comments = (List<Map<String,Object>>) obj;
				for(Map<String,Object> comment:comments) {
					
					//存放组装数据
					Map<String,Object> tempMap = new HashMap<String,Object>();
					//评论人昵称
					if(comment.containsKey("uname")) {
						String uname = comment.get("uname").toString();
						tempMap.put(Constants.USERNAME, uname);
					}
					
					//评论时间
					if(comment.containsKey("dtm")) {
						String time = comment.get("dtm").toString();
						time = ConstantFunc.normalTime(time);
						tempMap.put(Constants.COMMENT_TIME, time);
					}
					
					//评论内容
					if(comment.containsKey("msg")) {
						String content = comment.get("msg").toString();
						tempMap.put(Constants.COMMENT_CONTENT, content);
					}
					
					//评论顶的人数
					if(comment.containsKey("supportNum")) {
						int supportNum = Integer.parseInt(comment.get("supportNum").toString());
						tempMap.put(Constants.UP_CNT, supportNum);
					}
					
					//评论踩的人数
					if(comment.containsKey("againstNum")) {
						int againstNum = Integer.parseInt(comment.get("againstNum").toString());
						tempMap.put(Constants.UP_CNT, againstNum);
					}
					
					//回复
					if(comment.containsKey("reply")&& !comment.get("reply").equals("")) {
						Object reply = comment.get("reply");
						if(reply instanceof List) {
							List<Map<String,Object>> refers = (List<Map<String,Object>>)reply;
							for(Map<String,Object> referMap:refers)  {
								Map<String,Object> tempreferMap = new HashMap<String,Object>();
								//引用回复人昵称
								if(referMap.containsKey("uname")) {
									String uname = referMap.get("uname").toString();
									tempreferMap.put(Constants.REFER_COMM_USERNAME, uname);
								}
								
								//评论内容
								if(referMap.containsKey("msg")) {
									String content = referMap.get("msg").toString();
									tempreferMap.put(Constants.REFER_COMM_CONTENT, content);
								}
								refers.add(tempreferMap);
							}
							tempMap.put(Constants.REFER_COMMENTS, refers);
						}
					}
					commentList.add(tempMap);
				}
				parsedata.put(Constants.COMMENTS, commentList);
			}
		} catch (Exception e) {
			LOG.error("excuteParse error url:" + url);
		}
	}
}
