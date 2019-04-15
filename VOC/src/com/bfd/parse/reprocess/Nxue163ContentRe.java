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
 * 	@site：中国学网
 * 	@function：新闻内容页后处理
 * 	@author bfd_04
 *
 */
public class Nxue163ContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(Nxue163ContentRe.class);
//	private static final Pattern KINDID_PATTERN = Pattern.compile("kindid=(\\d+)");
//	private static final Pattern ARTICLEID_PATTERN = Pattern.compile("(\\d+).html");
//	private static final String URL_HEAD = "http://comments.cnmo.com/comments2012.php?";
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		Map<String, Object> resultData = new HashMap<String,Object>();
		Map<String, Object> processdata = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
		if (resultData != null) {
			/**
			 *       "cate": [
                "中国学网 > 站长之家 > 业界新闻 > 热点关注 > 正文"
        ]
			 */
			 if(resultData.containsKey(Constants.CATE)) {
				 List cate = (List)resultData.get("cate");
				 String[] cateArr = cate.get(0).toString().split(">");
				 cate.clear();          //clear old cate
				 for(String temp : cateArr) {
					 cate.add(temp.trim());
				 }
				 resultData.put(Constants.CATE, cate);
			 }
			/**
			 *   "source": "来源：互联网", 
			 */
			 if(resultData.containsKey(Constants.SOURCE)) {
				 String source = resultData.get(Constants.SOURCE).toString();
				 source = source.replace("来源：", "").trim();
				 resultData.put(Constants.SOURCE, source);
			 }
			 /**
			  * "author": "责任编辑：鲁晓倩"
			  */
			 if(resultData.containsKey(Constants.AUTHOR)) {
				 String author = resultData.get(Constants.AUTHOR).toString();
				 author = author.replace("责任编辑：", "").trim();
				 resultData.put(Constants.AUTHOR, author);
			 }
			 /**
			  * "post_time": "发表时间：2015/5/7 11:06:44"
			  */
			 if(resultData.containsKey(Constants.POST_TIME)) {
				 String postTime = resultData.get(Constants.POST_TIME).toString();
				 postTime = postTime.replace("发表时间：", "").trim();
				 resultData.put(Constants.POST_TIME, postTime);
			 }
////			 //deal with comment
//			 Map<String, Object> commentTask= new HashMap<String, Object>();
//			 String url = unit.getUrl();
//			 String pageData = unit.getPageData();
//			 Matcher match1 = KINDID_PATTERN.matcher(pageData);
//			 Matcher match2 = ARTICLEID_PATTERN.matcher(url);
//			 if(match1.find() && match2.find()) {
//				 try{
//					 String kindId = match1.group(1);
//					 String articleId = match2.group(1);
//					 StringBuilder sb = new StringBuilder();
//					 sb.append(URL_HEAD).append("kindid=").append(kindId).append("&articleid=").append(articleId);
//					 String comm_url = sb.toString();
//					 commentTask.put("link", comm_url);
//					 commentTask.put("rawlink", comm_url);
//					 commentTask.put("linktype", "newscomment");
////					 LOG.info("url:" + unit.getUrl() + "taskdata is "
////							 + commentTask.get("link") + commentTask
////							 .get("rawlink") + commentTask.get("linktype"));
//					resultData.put(Constants.COMMENT_URL, comm_url);
//					if(resultData.containsKey("tasks")) {
//						List<Map> tasks = (List<Map>) resultData.get("tasks");
//						tasks.add(commentTask);	
//					}
//					ParseUtils.getIid(unit, result);
//				 } catch (Exception e) {
////					e.printStackTrace(); 
//					LOG.debug(e.toString());
//				 }
//			 }
////			 LOG.info("url:" + unit.getUrl() + ".after reprocess rs is "
////						+ JsonUtil.toJSONString(resultData));
		}
		return new ReProcessResult(SUCCESS, processdata);
	}
}
