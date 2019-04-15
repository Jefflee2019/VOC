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
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;
/**
 * 站点名：财经网新闻
 * @author bfd_05
 *
 */
public class NcaijingCommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NcaijingCommentJson.class);
	
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
				executeParse(parsedata, json, data.getUrl());
			} catch (Exception e) {
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
	private void executeParse(Map<String, Object> parsedata, String json,
			String url) {
		Object obj = null;
		try {
			obj = JsonUtil.parseObject(json);
		} catch (Exception e) {
			LOG.error("jsonparser reprocess error url:" + url);
		}
		if(obj instanceof Map){
			Map<String, Object> map = (Map<String, Object>) obj;
			List<Map<String, Object>> comments = new ArrayList<Map<String, Object>>();
			parsedata.put(Constants.COMMENTS, comments);
			for(int i = 0; i< map.size() - 1; i++){
				Map<String, Object> storeMap = new  HashMap<String, Object>();
				Object commentObj = map.get(String.valueOf(i));
				if(commentObj instanceof Map){
					Map<String, Object> comment = (Map<String, Object>) commentObj; 
					//评论人
					if(comment.containsKey("nickname")){
						storeMap.put(Constants.USERNAME, comment.get("nickname"));
					}
					//评论内容
					if(comment.containsKey("orig_content")){
						storeMap.put(Constants.COMMENT_CONTENT, comment.get("orig_content"));
					}
					//评论时间
					if(comment.containsKey("date")){
						String comm_time = ConstantFunc.convertTime(comment.get("date").toString());
						storeMap.put(Constants.COMMENT_TIME, comm_time.trim());
					}
					//顶赞数
					if(comment.containsKey("supports")){
						storeMap.put(Constants.UP_CNT, comment.get("supports"));
					}
					comments.add(storeMap);
				}
			}
			if(map.containsKey("total")){
				int total = Integer.valueOf(map.get("total").toString());
				parsedata.put(Constants.COMMENT_REPLY_CNT, total);
				Pattern p = Pattern.compile("page=(\\d+)");
				Matcher mch = p.matcher(url);
				if(mch.find()){
					int page = Integer.valueOf(mch.group(1));
					if(page < total/10 + 1){
						String nextpage = url.replace(mch.group(), "page=" + (page + 1));
						Map<String, Object> nextTask = new HashMap<>();
						nextTask.put(Constants.LINK, nextpage);
						nextTask.put(Constants.RAWLINK, nextpage);
						nextTask.put(Constants.LINKTYPE, "newscomment");
						List<Map<String,Object>> tasks = new ArrayList<Map<String,Object>>();
						tasks.add(nextTask);
						parsedata.put(Constants.TASKS, tasks);
					}
				}
			}
		}
	}
	
}
