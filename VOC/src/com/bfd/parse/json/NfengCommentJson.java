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
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

public class NfengCommentJson implements JsonParser{
	
	private static final Log LOG = LogFactory.getLog(NfengCommentJson.class);
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient normalizerClient,
			ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parseData = new HashMap<String, Object>();
		// 遍历dataList
		for (JsonData jsonData : dataList) {
			if (!jsonData.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(jsonData, unit);
			try {
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
						&& (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["),
							json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"),
							json.lastIndexOf("}") + 1);
				}
				executeParse(parseData, json, jsonData.getUrl(), unit);
			} catch (Exception e) {
				parsecode = 500012;
				LOG.warn(
						"JsonParser exception, taskdata url="
								+ taskdata.get("url") + ".jsonUrl :"
								+ jsonData.getUrl(), e);
			}
		}
		// 组装返回结果
		JsonParserResult result = new JsonParserResult();
		try {
			result.setParsecode(parsecode);
			result.setData(parseData);
		} catch (Exception e) {
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}
	
	
	/**
	 * 处理新闻评论
	 * @param parsedata
	 * @param json
	 * @param url
	 * @param unit
	 */
	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parseData,
			String json, String url, ParseUnit unit){
		Map obj1 = null;
		try {
			List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
			parseData.put("tasks", taskList);
			obj1 = (Map) JsonUtil.parseObject(json);
		} catch (Exception e) {
			LOG.error("NcqnewsComment convert json error" );
		}
		String result = (String) obj1.get("result");
		if ("success".equals(result)) {
			int pageIndex = (int) obj1.get("page");
			int page_count = (int) obj1.get("page_count");
			int total_count = (int) obj1.get("total_count");//总数
			List comments = (List) obj1.get("list");//评论
			try {
				Map<Integer, Map> children_comments = (Map<Integer, Map>) obj1.get("children");//评论
				for (Map v : children_comments.values()) {
					comments.add(v);
				}
			} catch (Exception e){
				LOG.error("refer_comments is null");
			}
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();//评论
			if (total_count != 0) {
				//拼接下一页链接
				if (pageIndex < page_count) {
					String tt = getCresult(url, "page=(\\d+)");
					String offset = Integer.parseInt(tt) + 1 + "";
					String nextPage = url.replaceAll(getCresult1(url, "page=(\\d+)"), "page="+offset);
					initTask(parseData, nextPage);
				}
				
				for (Object object : comments) {
					Map obj = (Map) object;
					Map comment = new HashMap();//评论
					String comment_content = (String) obj.get("content");//内容
					String comment_time = (String) obj.get("add_time");//回复日期
					int up_cnt = (int) obj.get("score_1");//顶！d=====(￣▽￣*)b
					int down_cnt = (int) obj.get("score_2");//踩！d=====(￣▽￣*)b
					String username = (String) obj.get("user_name");//作者
					comment.put(Constants.COMMENT_CONTENT, comment_content);
					comment.put(Constants.COMMENT_TIME, comment_time);
					comment.put(Constants.UP_CNT, up_cnt);
					comment.put(Constants.DOWN_CNT, down_cnt);
					comment.put(Constants.USERNAME, username);
					list.add(comment);
				}
			}
			parseData.put(Constants.COMMENTS, list);
			parseData.put(Constants.REPLY_CNT, total_count);
		}
	}
	/**
	 * 正则匹配字符串
	 * @param str
	 * @param pattern
	 * @return
	 */
	private String getCresult(String str,String reg){
		Pattern pattern = Pattern.compile(reg);
		Matcher mch = pattern.matcher(str);
		if(mch.find()){
			return mch.group(1);
		}
		return str;
	}
	/**
	 * 正则匹配字符串
	 * @param str
	 * @param pattern
	 * @return
	 */
	private String getCresult1(String str,String reg){
		Pattern pattern = Pattern.compile(reg);
		Matcher mch = pattern.matcher(str);
		if(mch.find()){
			return mch.group(0);
		}
		return str;
	}
	/**
	 * 转换时间戳
	 * @param timestampString
	 * @return
	 */
	private String TimeStamp2Date(String timestampString){  
	    Long timestamp = Long.parseLong(timestampString)*1000;  
	    String date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(timestamp));  
	    return date;  
	} 
	
	/**
	 * 组装task
	 * @param parsedata
	 * @param nextPage
	 */
	@SuppressWarnings("unchecked")
	private void initTask(Map<String, Object> parsedata, String nextPage) {
		List<Map<String, Object>> taskList = (List<Map<String, Object>>) parsedata.get("tasks");
		Map<String, Object> nextpageTask = new HashMap<String, Object>();
		nextpageTask.put("link", nextPage);
		nextpageTask.put("rawlink", nextPage);
		nextpageTask.put("linktype", "newscomment");
		
		taskList.add(nextpageTask);
		parsedata.put("nextpage", nextpageTask);
		parsedata.put("tasks", taskList);
	}
}
