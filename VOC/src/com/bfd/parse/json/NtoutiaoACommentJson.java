package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

public class NtoutiaoACommentJson implements JsonParser{
	
	private static final Log LOG = LogFactory.getLog(NtoutiaoACommentJson.class);
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
		String message = (String) obj1.get("message");
		if ("success".equals(message)) {
			Map data = (Map) obj1.get("data");
			boolean has_more = (boolean) data.get("has_more");//是否下一页
			int total = (int) data.get("total");//总数
			List comments = (List) data.get("comments");//评论
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();//评论
			if (total != 0) {
				//拼接下一页链接
				if (has_more) {
					String tt = getCresult(url, "offset=(\\d+)");
					String offset = Integer.parseInt(tt) + 10 + "";
					String nextPage = url.replaceAll(getCresult1(url, "offset=(\\d+)"), "offset="+offset);
					initTask(parseData, nextPage);
				}
				
				for (Object object : comments) {
					Map obj = (Map) object;
					Map user = (Map) obj.get("user");
					Map comment = new HashMap();//评论
					String comment_content = (String) obj.get("text");//内容
					int time = (int) obj.get("create_time");//回复日期
					String comment_time = TimeStamp2Date(time + "");//回复日期
					int up_cnt = (int) obj.get("digg_count");//顶！d=====(￣▽￣*)b
					String username = (String) user.get("name");//作者
					comment.put(Constants.COMMENT_CONTENT, comment_content);
					comment.put(Constants.COMMENT_TIME, comment_time);
					comment.put(Constants.UP_CNT, up_cnt);
					comment.put(Constants.USERNAME, username);
					list.add(comment);
				}
			}
			parseData.put(Constants.COMMENTS, list);
			//评论总数
//			https://www.toutiao.com/a6595945313016480270
//			https://www.toutiao.com/api/comment/list/?group_id=6595945313016480270&item_id=6595945313016480270&offset=10&count=10
			String sttr = getCresult(url, "group_id=(\\d+)");
			String sourceData = gethtml("https://www.toutiao.com/a" + sttr);
			String buffer = this.getCresult1(sourceData, "commentInfo: \\{[\\s\\S]*?\\}");
			String reply_cnt = this.getCresult(buffer, "comments_count: (\\d+),");
			parseData.put(Constants.REPLY_CNT, reply_cnt);
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
	
	/**
	 * 下载页面内容
	 */
	public static String gethtml (String url) {
		String htmlContent = "";
		HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient client = builder.build();
		HttpGet request = new HttpGet(url);
		request.setHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36");
		try {
			HttpResponse response = client.execute(request);
			htmlContent = EntityUtils.toString(response.getEntity(),"utf-8");
//			System.err.println(htmlContent);
			return htmlContent;
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}
}
