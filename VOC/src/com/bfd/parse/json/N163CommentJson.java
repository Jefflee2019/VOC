package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

public class N163CommentJson implements JsonParser{
	
	private static final Log LOG = LogFactory.getLog(N163CommentJson.class);
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
		Map message = (Map) obj1.get("comments");
		if (!message.isEmpty()) {
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();//评论
			for(Object obj : message.values()){
				Map map = (Map) obj;
				Map comment = new HashMap();//评论
				Map user = (Map) map.get("user");
				String comment_content = (String) map.get("content");//内容
				String comment_time = (String) map.get("createTime");//回复日期
				int up_cnt = (int) map.get("vote");//顶
				String username = (String) user.get("nickname");//作者 
				String location = (String) user.get("location");//所在地 
				comment.put(Constants.COMMENT_CONTENT, comment_content);
				comment.put(Constants.COMMENT_TIME, comment_time);
				comment.put(Constants.UP_CNT, up_cnt);
				if (username == null) {
					comment.put(Constants.USERNAME, "网易" + location + "网友");
				}else{
					comment.put(Constants.USERNAME, username);
				}
				list.add(comment);
			}
			parseData.put(Constants.COMMENTS, list);
		}
		//下载回复数，参与数 https://comment.api.163.com/api/v1/products/a2869674571f77b5a0867c3d71db5856/threads/DMU0FNR800097U7S
		String pv_url = "https://comment.api.163.com/api/v1/products/a2869674571f77b5a0867c3d71db5856/threads/source_id";
		String pv_id = getCresult(url, "threads/(\\w+)/comments");
		String pv_str = this.gethtml(pv_url.replaceAll("source_id", pv_id));
		try {
			Map res = (Map) JsonUtil.parseObject(pv_str);
			int tcount = (int) res.get("tcount");//回复数
			int rcount = (int) res.get("rcount");
			int cmtVote = (int) res.get("cmtVote");
			int cmtAgainst = (int) res.get("cmtAgainst");
			int partake_cnt = rcount + cmtVote + cmtAgainst;//参与数
			int vote = (int) res.get("vote");//点赞数
			parseData.put(Constants.REPLY_CNT, tcount);
			parseData.put(Constants.PARTAKE_CNT, partake_cnt);
			parseData.put(Constants.UP_CNT, vote);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
