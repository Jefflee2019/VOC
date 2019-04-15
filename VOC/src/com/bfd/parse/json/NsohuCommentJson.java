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

public class NsohuCommentJson implements JsonParser{
	
	private static final Log LOG = LogFactory.getLog(NsohuCommentJson.class);
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
		String message = (String) obj1.get("msg");
		if ("SUCC".equals(message)) {
			Map jsonObject = (Map) obj1.get("jsonObject");
			int participation_sum = (int) jsonObject.get("participation_sum");//参与数
			int cmt_sum = (int) jsonObject.get("cmt_sum");//评论数
			int total_page_no = (int) jsonObject.get("total_page_no");//总页数
			List comments = (List) jsonObject.get("comments");//评论
			List hots = (List) jsonObject.get("hots");//热门评论
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();//评论
			if (cmt_sum != 0) {
				//拼接下一页链接
				String tt = getCresult(url, "page_no=(\\d+)");
				if (total_page_no > 1 && Integer.parseInt(tt) < total_page_no) {
//					http://apiv2.sohu.com/api/topic/load?page_size=10&page_no=1&hot_size=5&source_id=mp_240672115
					String page_no = Integer.parseInt(tt) + 1 + "";
					String nextPage = url.replaceAll(getCresult1(url, "page_no=(\\d+)"), "page_no="+page_no);
					initTask(parseData, nextPage);
				}
				
				for (Object object : comments) {
					Map obj = (Map) object;
					Map user = (Map) obj.get("passport");
					Map comment = new HashMap();//评论
					String comment_content = (String) obj.get("content");//内容
					long time = (long) obj.get("create_time");//回复日期
					String comment_time = TimeStamp2Date(time);//回复日期
					int up_cnt = (int) obj.get("support_count");//点赞数
					int com_reply_cnt = (int) obj.get("reply_count");//回复数
					String username = (String) user.get("nickname");//作者
					comment.put(Constants.COMMENT_CONTENT, comment_content);
					comment.put(Constants.COMMENT_TIME, comment_time);
					comment.put(Constants.UP_CNT, up_cnt);
					comment.put(Constants.COM_REPLY_CNT, com_reply_cnt);
					comment.put(Constants.USERNAME, username);
					list.add(comment);
				}
			}
			parseData.put(Constants.COMMENTS, list);
			parseData.put(Constants.REPLY_CNT, cmt_sum);
			parseData.put(Constants.PARTAKE_CNT, participation_sum);
			//下载阅读数  http://v2.sohu.com/public-api/articles/240757613/pv
			String pv_url = "http://v2.sohu.com/public-api/articles/source_id/pv";
			String pv_id = getCresult(url, "source_id=mp_(\\d+)");
			String pv_str = this.gethtml(pv_url.replaceAll("source_id", pv_id));
			parseData.put(Constants.VIEW_CNT, pv_str);//阅读数
		}
	}
	
	/**
	 * 下载
	 */
	public static String gethtml (String url) {
		String htmlContent = "";
		HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient client = builder.build();
		HttpGet request = new HttpGet(url);
		request.setHeader("User-Agent","Dalvik/2.1.0 (Linux; U; Android 7.1.1; vivo X20A Build/NMF26X) NewsArticle/6.7.8 cronet/58.0.2991.0@59592eaa");
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
	private String TimeStamp2Date(Long timestampString){  
	    String date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(timestampString));  
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
