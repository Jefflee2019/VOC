package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.entity.Constants;
import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
/**
 * 	@site：砍柴网
 * 	@function：新闻内容页后处理
 * 	@author bfd_04
 *
 */
public class NikanchaiContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NikanchaiContentRe.class);
	private static final Pattern APPID = Pattern.compile("appid = '(.*?)'"); //var appid = 'cyraSkQnY'
	private static final String COMM_URL_HEAD = "http://changyan.sohu.com/node/html?client_id=";
	private static final Pattern DATE_PATTERN = Pattern.compile("([0-9]{4}-[0-9]{2}-[0-9]{2}\\s+[0-9]{2}:[0-9]{2})");
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		Map<String, Object> resultData = new HashMap<String,Object>();
		Map<String, Object> processdata = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
		if (resultData != null) {
			/**
			 *     "cate": [
                "科技快报 > 新闻 > 论！华为逆袭小米之路！"
        ], 
			 */
			 if(resultData.containsKey(Constants.CATE)) {
				 List cate = (List)resultData.get("cate");
				 String[] cateArr = cate.get(0).toString().split(" > ");
				 cate.clear();          //clear old cate
				 for(String temp : cateArr) {
					 cate.add(temp.trim());
				 }
				 resultData.put(Constants.CATE, cate);
			 }
			/**
			 *  "author": "作者：雷宇辉", 
			 */
			if(resultData.containsKey(Constants.AUTHOR)) {
				String author = resultData.get(Constants.AUTHOR).toString();
				author = author.replace("作者：", "").trim();
				
				resultData.put(Constants.AUTHOR, author);
			}
			/**
			 *   "source": "/ 东方财富网 / 2016-03-13 06:04"
			 *   source": "出处：未知",
			 */
			 if(resultData.containsKey(Constants.SOURCE)) {
				 String source = resultData.get(Constants.SOURCE).toString();
				 if (source.contains("出处：")) {
					 source = source.replace("出处：", "").trim();
				 }
				 if(source.contains("/ ")) {
					 source = source.split("/ ")[1].trim();
				 }
				 resultData.put(Constants.SOURCE, source);
			 }
			 /**
			  *   "post_time": "/ 东方财富网 / 2016-03-13 06:04"
			  */
			 if(resultData.containsKey(Constants.POST_TIME)) {
				 String postTime = resultData.get(Constants.POST_TIME).toString();
				 Matcher dateMatcher = DATE_PATTERN.matcher(postTime);
				 if(dateMatcher.find()) {
					 postTime = dateMatcher.group(1);
				 }
				 
				 resultData.put(Constants.POST_TIME, postTime);
			 }
//			 //deal with comment
			 Map<String, Object> commentTask= new HashMap<String, Object>();
			 String url = unit.getUrl();
			 String pageData = unit.getPageData();
			 Matcher match1 = APPID.matcher(pageData);
			 if(match1.find()) {
				 try{
					 String appId = match1.group(1);
					 StringBuilder sb = new StringBuilder();
//					 sb.append(COMM_URL_HEAD).append(appId).append("&title=")
//					 	.append(resultData.get("title").toString()).append("&topicurl=")
//					 	.append(url);
					 sb.append(COMM_URL_HEAD).append(appId).append("&topicurl=").append(url);
					 String comm_url = sb.toString();
					 commentTask.put("link", comm_url);
					 commentTask.put("rawlink", comm_url);
					 commentTask.put("linktype", "newscomment");
//					 LOG.info("url:" + unit.getUrl() + "taskdata is "
//							 + commentTask.get("link") + commentTask
//							 .get("rawlink") + commentTask.get("linktype"));
					resultData.put(Constants.COMMENT_URL, comm_url);
					if(resultData.containsKey("tasks")) {
						List<Map> tasks = (List<Map>) resultData.get("tasks");
						tasks.add(commentTask);	
					}
					ParseUtils.getIid(unit, result);
				 } catch (Exception e) {
//					e.printStackTrace(); 
					LOG.debug(e.toString());
				 }
			 }
//			 LOG.info("url:" + unit.getUrl() + ".after reprocess rs is "
//						+ JsonUtil.toJSONString(resultData));
		} else {
//			LOG.info("url:" + unit.getUrl() + "result.getParsedata().getData() is null");
		}
		return new ReProcessResult(SUCCESS, processdata);
	}
}
