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
 * 	@site：中关村游戏
 * 	@function：新闻内容页后处理
 * 	@author bfd_04
 *
 */
public class NzolgameContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NzolgameContentRe.class);
//	private static final Pattern ARTICLE_ID = Pattern.compile("(\\d+).html"); //5727831.html
	private static final Pattern IDS = Pattern.compile("kindid=(\\d+)&articleid=(\\d+)");//kindid=8&articleid=5731964&from=6"
	private static final Pattern DATE_PATTERN = Pattern.compile("([0-9]{4}年[0-9]{2}月[0-9]{2}日\\s+[0-9]{2}:[0-9]{2})");//中关村在线 2016年03月23日 15:49
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		Map<String, Object> resultData = new HashMap<String,Object>();
		Map<String, Object> processdata = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
		if (resultData != null) {
			/**
			 * "author": "作者： 马玥", 
			 */
			if(resultData.containsKey(Constants.AUTHOR)) {
				/**
				 * "author": "作者： 马玥", 
				 */
				String author = resultData.get(Constants.AUTHOR).toString();
				author = author.replace("作者：", "").trim();
				
				resultData.put(Constants.AUTHOR, author);
			}
			if(resultData.containsKey(Constants.POST_TIME)) {
				String postTime = resultData.get(Constants.POST_TIME).toString();
				if(postTime.contains("年") && postTime.contains("月")) {
					Matcher dateMatcher = DATE_PATTERN.matcher(postTime);
					if(dateMatcher.find()) {
						postTime = dateMatcher.group(1);
					}
				}
				
				resultData.put(Constants.POST_TIME, postTime);
			}
			/**
			 *          "cate": [
                "ZOL首页 > 家电频道 > 净化器 > 新闻 > 正文"
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
			 *   "source": " [ 中关村在线 原创 ]",
			 */
			 if(resultData.containsKey(Constants.SOURCE)) {
				 String source = resultData.get(Constants.SOURCE).toString();
				 if(source.contains("[")) {
					 source = source.replace("[", "").replace("]", "").trim().split(" ")[0];
				 }
				 resultData.put(Constants.SOURCE, source);
			 }
//			 //deal with comment
			 Map<String, Object> commentTask= new HashMap<String, Object>();
			 String url = unit.getUrl();
			 String pageData = unit.getPageData();
			 Matcher match1 = IDS.matcher(pageData);
			 if(match1.find()) {
				 try{
					 String kindid = match1.group(1);
					 String articleId = match1.group(2);
					 StringBuilder sb = new StringBuilder();
					 sb.append("http://comment.zol.com.cn/").append(kindid).append("/")
					 	.append(articleId).append("_0_0_1.html");
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
