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
import com.bfd.parse.util.TextUtil;
/**
 * 站点：Bchiphell
 * 功能:获取帖子页内容
 * @author dph 2017年12月15日
 *
 */
public class BchiphellPostJson implements JsonParser{
	private static final Log LOG = LogFactory.getLog(EthmallCommentJson.class);
	//一层回复
	private static final Pattern PATTERN_REPLY = Pattern.compile("<div id=\"post_\\d+\" >(.|\n)*?<div id=\"post");
	//主题
	private static final Pattern PATTERN_THREAD_SUBJECT = Pattern.compile("<span id=\"thread_subject\">((.|\n)*?)</span>");
	//回复内容
	private static final Pattern PATTERN_REPLYCONTENT = Pattern.compile("<div class=\"t_fsz\">(.|\n)*?</table>");
	//发表时间
	private static final Pattern PATTERN_POSTTIME = Pattern.compile("<em id=\"authorposton\\d+\">发表于((.|\n)*?)</em>");
	//楼层
	private static final Pattern PATTERN_REPLYFLOOR = Pattern.compile("<em>(\\d+)</em>");
	//回复者昵称
	private static final Pattern PATTERN_REPLYUSERNAME = Pattern.compile(""
			+ "<a href=\"space-uid-\\d+.html\" target=\"_blank\" class=\"xw1\"( style=\"color: #CC0000\">)*((.|\n)*?)</a>");
	//下一页
	private static final Pattern PATTERN_NEXTPAGE = Pattern.compile("<a href=\"(thread-\\d+-\\d+-1.html)\" class=\"nxt\">");
	
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient normalizerClient, ParseUnit unit) {
		Map<String,Object> parsedata = new HashMap<String,Object>(5);
		int parsecode = 0;
		for(Object obj : dataList){
			JsonData data = (JsonData) obj;
			if(!data.downloadSuccess()){
				continue;
			}
			//解压缩ajax数据
			String json = TextUtil.getUnzipJson(data, unit);
			executeParse(parsedata,json, unit);
		}
		JsonParserResult result = new JsonParserResult();
		try{
			result.setParsecode(parsecode);	
			result.setData(parsedata);
		}catch(Exception e){
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}
    @SuppressWarnings("unchecked")
	private void executeParse(Map<String, Object> parsedata, String json, ParseUnit unit) {
    	String htmlData = json;
    	List<Map<String, Object>> replyList =null;
		if(parsedata.get(Constants.REPLYS) != null){
			replyList = (List<Map<String,Object>>) parsedata.get(Constants.REPLYS);					
		}else{
			replyList = new ArrayList<Map<String,Object>>();
		}
		List<Map<String, Object>> taskList =null;
		if(parsedata.get(Constants.TASKS) != null){
			taskList = (List<Map<String,Object>>) parsedata.get(Constants.TASKS);					
		}else{
			taskList = new ArrayList<Map<String,Object>>();
		}
    	Matcher replyMatcher = PATTERN_REPLY.matcher(htmlData);
    	while(replyMatcher.find()){
    		Map<String,Object> replyMap =new HashMap<String,Object>(4);
    		String reply = replyMatcher.group(0);
    		//获取回复内容
    		Matcher replyContentM = PATTERN_REPLYCONTENT.matcher(reply);
    		String replyContent = null;
    		while(replyContentM.find()){
    			replyContent = replyMatcher.group(0);
    			replyContent = replyContent.replaceAll("<(.|\n)*?>", "");
    		}
    		//获取发表时间
    		Matcher posttimeM = PATTERN_POSTTIME.matcher(reply);
    		String posttime = null;
    		while(posttimeM.find()){
    			posttime = posttimeM.group(1);
    		}
    		//获取昵称
    		Matcher replyusernameM = PATTERN_REPLYUSERNAME.matcher(reply);
    		String username = null;
    		while(replyusernameM.find()){
    			username = replyusernameM.group(2);
    		}
    		//获取楼层
    		Matcher replyfloorM = PATTERN_REPLYFLOOR.matcher(reply);
    		String replyfloor = null;
    		while(replyfloorM.find()){
    			replyfloor = replyfloorM.group(1);
    		}
    		if(replyfloor.equals("1")){
    			parsedata.put(Constants.CONTENTS, replyContent);
    			parsedata.put(Constants.NEWSTIME, posttime);
    			parsedata.put(Constants.AUTHORNAME, username);
    			continue;
    		}
    		replyMap.put(Constants.POSTTIME, posttime);
    		replyMap.put(Constants.REPLYFLOOR, replyfloor);
    		replyMap.put(Constants.REPLYUSERNAME, username);
    		replyMap.put(Constants.REPLYCONTENT, replyContent);
    		replyList.add(replyMap);
    	}
    	//获取主题
    	Matcher titleM = PATTERN_THREAD_SUBJECT.matcher(htmlData);
    	while(titleM.find()){
    		String title = titleM.group(1);
    		parsedata.put(Constants.TITLE, title);
    	}
    	//获取下一页
    	Matcher pageM = PATTERN_NEXTPAGE.matcher(htmlData);
    	while(pageM.find()){
    		String page = pageM.group(1);
    		Map<String,Object> nextpageMap = new HashMap<>();
    		String link = "https://www.chiphell.com/" + page;
    		nextpageMap.put(Constants.LINK, link);
    		nextpageMap.put(Constants.RAWLINK, link);
    		nextpageMap.put(Constants.LINKTYPE, "bbspost");
    		parsedata.put(Constants.NEXTPAGE, link);
    		taskList.add(nextpageMap);
    		parsedata.put(Constants.TASKS, taskList);
    		return;
    	}
    	parsedata.put(Constants.REPLYS, replyList);
	
    }

}
