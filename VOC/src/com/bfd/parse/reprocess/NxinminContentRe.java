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
 * 	@site：新民网(Nxinmin)
 * 	@function：新闻内容页后处理
 * 	@author bfd_04
 *
 */
public class NxinminContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NxinminContentRe.class);
	private static final Pattern PATTERN = Pattern.compile("(\\d+).html");
	private static final String COMM_URL_HEAD = "http://comment.xinmin.cn/index.php?app=common&controller=Comment.List&indexId=";
	private static final Pattern DATE_PATTERN = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}\\s+?([0-9]{2}:[0-9]{2}(:[0-9]{2})?)?");
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		Map<String, Object> resultData = new HashMap<String,Object>();
		Map<String, Object> processdata = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
		String url = unit.getUrl();
		String title = "";
		Matcher dateMatcher = null;
		if (resultData != null) {
			if(resultData.containsKey(Constants.TITLE)) {
				title = resultData.get(Constants.TITLE).toString();
			}
			/**
			 *    "cate": [
                		"您现在的位置： 首页 > 新闻 > 正文"
        			], 
			 */
			 if(resultData.containsKey(Constants.CATE)) {
				 List cateList = (List) resultData.get(Constants.CATE);
				 if(cateList !=null && !cateList.isEmpty() ) {
					 String cate = (String) cateList.get(0);
					 cate = cate.replace("您现在的位置：", "");
					 String[] strArr = cate.split(">");
					 cateList.clear();
					 for(String tempStr: strArr) {
						 cateList.add(tempStr);
					 }
				 }
			 }
			 
			if(url.contains("http://tech.xinmin.cn/it")) {
				 /**
				  * "post_time": "2006-11-21 13:43 来源：新浪科技 作者：曹增辉 进入论坛 共 0 条评论"
				  */
				 if(resultData.containsKey(Constants.POST_TIME)) {
					 String postTime = resultData.get(Constants.POST_TIME).toString();
					 dateMatcher = DATE_PATTERN.matcher(postTime);
					 if(dateMatcher.find()) {
						 postTime = dateMatcher.group();
					 } else {
						 postTime = "";
					 }
					 resultData.put(Constants.POST_TIME, postTime);
				 }
				 
				 /**
					 * "author": "作者：程久龙", 
					 */
					 if(resultData.containsKey(Constants.AUTHOR)) {
						 String author = resultData.get(Constants.AUTHOR).toString();
//						 author = author.replace("作者：", "").trim();
						 String[] authorArr = author.split("作者：");
						 if(authorArr.length > 1) {
							 author = authorArr[1].split(" ")[0];
						 } else {
							 author = "";
						 }
						 resultData.put(Constants.AUTHOR, author);
					 }
					 /**
					  * "source": "来源：21世纪经济", 
					  */
					 if(resultData.containsKey(Constants.SOURCE)) {
						 String source = resultData.get(Constants.SOURCE).toString();
//						 source = source.replace("来源：", "").trim();
						 String[] sourceArr = source.split("来源：");
						 if(sourceArr.length > 1) {
							 source = sourceArr[1].split(" ")[0].trim();
						 } else {
							 source = "";
						 }
						 resultData.put(Constants.SOURCE, source);
					 }
			} else {
				/**
				 * "author": "作者：程久龙", 
				 */
				 if(resultData.containsKey(Constants.AUTHOR)) {
					 String author = resultData.get(Constants.AUTHOR).toString();
					 author = author.replace("作者：", "").trim();
					 resultData.put(Constants.AUTHOR, author);
				 }
				 /**
				  * "source": "来源：21世纪经济", 
				  */
				 if(resultData.containsKey(Constants.SOURCE)) {
					 String source = resultData.get(Constants.SOURCE).toString();
					 source = source.replace("来源：", "").trim();
					 resultData.put(Constants.SOURCE, source);
				 }
			}
			
//			 //deal with comment
			 Map<String, Object> commentTask= new HashMap<String, Object>();
//			 String url = unit.getUrl();
			 Matcher match = PATTERN.matcher(url);
			 if(match.find()) {
				 try{
					 String articleId = match.group(1);
					 StringBuilder sb = new StringBuilder();
					 url = java.net.URLEncoder.encode(url,"utf-8");
					 sb.append(COMM_URL_HEAD).append(articleId).append("&title=")
					 	.append(title).append("&url=").append(url);
					 String commUrl = sb.toString();
					 commentTask.put("link", commUrl);
					 commentTask.put("rawlink", commUrl);
					 commentTask.put("linktype", "newscomment");
					 resultData.put(Constants.COMMENT_URL, commUrl);
					if(resultData.containsKey("tasks")) {
						List<Map> tasks = (List<Map>) resultData.get("tasks");
						tasks.add(commentTask);	
					}
				 } catch (Exception e) {
					 LOG.debug(e.toString());
				 }
			 }
		} 
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
