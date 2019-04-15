package com.bfd.parse.reprocess;

import java.util.Arrays;
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
 * 	@site：C114(N114)
 * 	@function：新闻内容页后处理
 * 	@author bfd_04
 *
 */
public class N114ContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(N114ContentRe.class);
	private static final Pattern ARTICLE_ID_PATTERN = Pattern.compile("a(\\d+).html");
	private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{4}/\\d{1,2}/\\d+{1,2}\\s+\\d{2}:\\d+{2}(:\\d+{2})?)");
	private static final Pattern AUTHOR_PATTERN = Pattern.compile("作者：(.*?)\\s+?");
	private static final Pattern SOURCE_PATTERN = Pattern.compile("来源：(.*?)\\s+?");
	private static final String COMM_URL_HEAD = "http://www.c114.com.cn/comment/?article=";
	private static Matcher match = null;
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		Map<String, Object> resultData = new HashMap<String,Object>();
		Map<String, Object> processdata = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
		if (resultData != null) {
			/**
			 *  "author": " 作者："
			 */
			 if(resultData.containsKey(Constants.AUTHOR)) {
				 String author = resultData.get(Constants.AUTHOR).toString();
				 match = AUTHOR_PATTERN.matcher(author);
				 if(match.find()) {
					 author = match.group(1);
				 } else {
					 author = "";
				 }
				 resultData.put(Constants.AUTHOR, author);
			 }
			 /**
			  *  "source": "来源：今晚报"
			  */
			 if(resultData.containsKey(Constants.SOURCE)) {
				 String source = resultData.get(Constants.SOURCE).toString();
				 match = SOURCE_PATTERN.matcher(source);
				 if(match.find()) {
					 source = match.group(1);
				 } else {
					 source = "";
				 }
				 resultData.put(Constants.SOURCE, source);
			 }
			 /**
			  *   post_time": "http://www.c114.com.cn ( 2015/10/30 18:09 )"
			  */
			 if(resultData.containsKey(Constants.POST_TIME)) {
				 String postTime = resultData.get(Constants.POST_TIME).toString();
				 match = DATE_PATTERN.matcher(postTime);
				 if(match.find()) {
					 postTime = match.group(1);
				 }
				 resultData.put(Constants.POST_TIME, postTime);
			 }
			 /**
			  *  "cate": [
                "新闻 - 电信运营商 - 中国电信 - 正文"
                     ], 
			  */
			 if(resultData.containsKey(Constants.CATE)) {
					List cate = (List)resultData.get(Constants.CATE);
					String[] temp = cate.get(0).toString().split("-");
					cate = Arrays.asList(temp);
					resultData.put(Constants.CATE, cate);
				}
			 /**
			  *  "keyword": "本文关键字: 中国电信 2, 电信 1, 路由器 6, 招标 2, 华为 2, 上海贝尔 2, 思科 1, 城域网 2, 互联网 1", 
			  */
			 if(resultData.containsKey(Constants.KEYWORD)) {
				 String keyword = (String)resultData.get(Constants.KEYWORD);
				 keyword = keyword.replace("本文关键字:", "").trim();
				 resultData.put(Constants.KEYWORD, keyword);
			 }
			 
//			 //deal with comment
			 Map<String, Object> commentTask= new HashMap<String, Object>();
			 String url = unit.getUrl();
			 Matcher match = ARTICLE_ID_PATTERN.matcher(url);
			 if(match.find()) {
				 try{
					 String articleId = match.group(1);
					 StringBuilder sb = new StringBuilder();
					 sb.append(COMM_URL_HEAD).append(articleId);
					 String commURL = sb.toString();
					 commentTask.put("link", commURL);
					 commentTask.put("rawlink", commURL);
					 commentTask.put("linktype", "newscomment");
					 resultData.put(Constants.COMMENT_URL, commURL);
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
		} 
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
