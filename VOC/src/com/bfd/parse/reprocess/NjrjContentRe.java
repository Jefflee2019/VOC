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
 * 	@site：金融界
 * 	@function：新闻内容页后处理
 * 	@author bfd_04
 *
 */
public class NjrjContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NjrjContentRe.class);
	private static final Pattern APPID = Pattern.compile("appId='(\\d+)'");
	private static final Pattern APPITEMID = Pattern.compile("iiid=(\\d+)");
	private static final String COMM_URL_HEAD = "http://news.comments.jrj.com.cn"+
			"/index.php/commentslist?appId=";
	private static final String COMM_URL_END = "&pageSize=10&page=1";
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		Map<String, Object> resultData = new HashMap<String,Object>();
		Map<String, Object> processdata = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
		if (resultData != null) {
			/**
			 * "cate": [
                "金融界首页 > 科技频道 > IT业界 > 正文"
        ], 
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
			 *  "source": "来源： 雷锋网", 
			 */
			 if(resultData.containsKey(Constants.SOURCE)) {
				 String source = resultData.get(Constants.SOURCE).toString();
				 source = source.replace("来源：", "").trim();
				 resultData.put(Constants.SOURCE, source);
			 }
			 /**
			  *  "author": "作者： Freeze-tian", 
			  */
			 if(resultData.containsKey(Constants.AUTHOR)) {
				 String author = resultData.get(Constants.AUTHOR).toString();
				 author = author.replace("作者：", "").trim();
				 resultData.put(Constants.AUTHOR, author);
			 }
//			 //deal with comment
			 Map<String, Object> commentTask= new HashMap<String, Object>();
			 String url = unit.getUrl();
			 String pageData = unit.getPageData();
			 Matcher match1 = APPID.matcher(pageData);
			 Matcher match2 = APPITEMID.matcher(pageData);
			 if(match1.find() && match2.find()) {
				 try{
					 String appId = match1.group(1);
					 String appItemId = match2.group(1);
					 StringBuilder sb = new StringBuilder();
					 sb.append(COMM_URL_HEAD).append(appId).append("&appItemid=")
					 	.append(appItemId).append(COMM_URL_END);
					 String comm_url = sb.toString();
					 commentTask.put("link", comm_url);
					 commentTask.put("rawlink", comm_url);
					 commentTask.put("linktype", "newscomment");
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
