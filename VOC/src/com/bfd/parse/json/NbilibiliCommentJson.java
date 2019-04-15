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

public class NbilibiliCommentJson implements JsonParser{
	
	private static final Log LOG = LogFactory.getLog(NbilibiliCommentJson.class);
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
		List<Map<String,Object>> commentList = new ArrayList<Map<String,Object>>();//评论
		Map data = (Map) obj1.get("data");
		if (data != null) {
			Map page = (Map) data.get("page");
			int count = (int) page.get("count");//总评论数
			int size = (int) page.get("size");
			if (count != 0) {
				int totalNo = count / size + 1; //总页数
				List replies = (List) data.get("replies");
				for (Object object : replies) {
					Map obj = (Map) object;
					Map member = (Map) obj.get("member");
					String uname = (String) member.get("uname");//作者
					Map content = (Map) obj.get("content");
					String message = (String) content.get("message");//内容
					int ctime = (int) obj.get("ctime");//时间
					Map comment = new HashMap();//评论
					comment.put(Constants.COMMENT_CONTENT, message);
					comment.put(Constants.COMMENT_TIME, this.TimeStamp2Date(ctime + ""));
					comment.put(Constants.USERNAME, uname);
					commentList.add(comment);
				}
				parseData.put(Constants.COMMENTS, commentList);
				parseData.put(Constants.REPLY_CNT, count);
				
//				https://api.bilibili.com/x/v2/reply?jsonp=jsonp&pn=1&type=1&oid=7886591&sort=0
				int pageIndex = Integer.parseInt(this.getCresult(url, "pn=(\\d+)"));
				//拼接下一页链接
				if (pageIndex < totalNo) {
					String offset = pageIndex + 1 + "";
					String nextPage = url.replaceAll(getCresult1(url, "pn=(\\d+)"), "pn="+offset);
					initTask(parseData, nextPage);
				}
			}
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
