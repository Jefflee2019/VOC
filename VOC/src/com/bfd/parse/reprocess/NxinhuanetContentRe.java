package com.bfd.parse.reprocess;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.ParseUtils;

/**
 * @site: 新华网新闻（Nxinhuanet）
 * @function: 新闻内容页后处理，获取评论页，数据标准化
 * @author BFD_499
 *
 */
public class NxinhuanetContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NxinhuanetContentRe.class);
	private static final String URLHEAD = "http://comment.home.news.cn/a/newsCommAll.do?callback=jsonp82&newsId=1-";
	private static final Pattern NEWSIDP = Pattern.compile("c_(\\d+).htm");
	private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{4}-\\d{1,2}-\\d{1,2})");

	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		String url = unit.getUrl();
		Matcher newsIdM = NEWSIDP.matcher(url);
		Map<String, Object> resultData = result.getParsedata().getData();
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> commentTask = new HashMap<String, Object>();
		
		if(resultData != null && !resultData.isEmpty()) {
			if(url.contains("bt.xinhuanet.com")) {
				parser02(resultData);
			} 
			else {
				parser01(resultData);
			}
		}
		
		
		/**
		 * 生成新闻评论
		 */
		if(newsIdM.find())
		{
			try {
				String newsId = newsIdM.group(1);
				String comUrl = URLHEAD + newsId;
				commentTask.put("link", comUrl);
				commentTask.put("rawlink", comUrl);
				commentTask.put("linktype", "newscomment");
				if (!resultData.isEmpty()) {
					resultData.put("comment_url", comUrl);
					List<Map> tasks = (List<Map>) resultData.get("tasks");
					tasks.add(commentTask);	
				}
			} catch (Exception e) {
//				e.printStackTrace();
				LOG.warn(url + "get comment_url error");
			}
		}
		
		ParseUtils.getIid(unit, result);
		LOG.info("url:" + unit.getUrl() + ".after reprocess rs is "
				+ JsonUtil.toJSONString(resultData));
		return new ReProcessResult(processcode, processdata);
	}
	
	private void parser02(Map<String,Object> resultData){
	
			/**
			 * 标准化时间 Constants.POST_TIME: "作者： 焦立坤 来源： 新华网 期： 2015-12-22"
			 */
		if(resultData.containsKey(Constants.POST_TIME))
		{
			String postTime = (String) resultData.get(Constants.POST_TIME);
			
			Matcher dateMatch = DATE_PATTERN.matcher(postTime);
			
			if(dateMatch.find()) {
				postTime = dateMatch.group(0);
			}
			resultData.put(Constants.POST_TIME, postTime);
//			LOG.info("url:" + unit.getUrl() + "post_time is " + post_time);
		}
		/**
		 * "作者： 焦立坤 来源： 新华网 日期： 2015-12-22",
		 */
		if(resultData.containsKey(Constants.AUTHOR))
		{
			String author = (String) resultData.get(Constants.AUTHOR);
			if (author.contains("作者：")) {
				String tempArr[] = author.split("作者：");
				if (tempArr.length > 1) {
					author = tempArr[1].trim().split(" ")[0];
				}
			}
			resultData.put(Constants.AUTHOR, author);
		}
		/**
		 * 处理来源 Constants.AUTHOR: "作者： 焦立坤 来源： 新华网 日期： 2015-12-22", 
		 */
		if(resultData.containsKey(Constants.SOURCE))
		{
			String source = (String) resultData.get(Constants.SOURCE);
			if (source.contains("来源：")) {
				String tempArr[] = source.split("来源：");
				if (tempArr.length > 1) {
					source = tempArr[1].trim().split(" ")[0];
				}
			}
			resultData.put(Constants.SOURCE, source);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void parser01(Map<String,Object> resultData){
		if(resultData.containsKey(Constants.CATE))
		{
			List cate = (List) resultData.get(Constants.CATE);
			if(!cate.isEmpty() && cate.get(0).toString().contains(">")) {
				String[] tmp = cate.get(0).toString().split(">");
				cate = Arrays.asList(tmp);
			}
			resultData.put(Constants.CATE, cate);
		}
	
		/**
		 * 处理作者[责任编辑：某某某]
		 */
		if(resultData.containsKey(Constants.AUTHOR))
		{
			String author = (String) resultData.get(Constants.AUTHOR);
			if(author.contains("[") && author.contains("[") && (author.contains(":") || author.contains("：")))
			{
				String[] authorStrs = author.split(":|：");
				int endIndex = authorStrs[1].indexOf("]");
				author = authorStrs[1].trim().substring(0, endIndex-1).trim();
			} else if(author.contains("作者：")){
				author = author.trim().split(" ")[0].replace("作者：", "").trim();
			}
			resultData.put(Constants.AUTHOR, author.replace("编辑: ", ""));
		}
		/**
		 * 处理来源 来源： 新华信息化
		 */
		if(resultData.containsKey(Constants.SOURCE))
		{
			String source = (String) resultData.get(Constants.SOURCE);
			if(source.contains(":") || source.contains("："))
			{
				String[] sourceStrs = source.split(":|：");
				resultData.put(Constants.SOURCE, sourceStrs[1].trim());
			}
			String[] sourceStrArr = source.split("来源：");
			if(sourceStrArr.length > 1) {
				source = sourceStrArr[1].trim().split(" ")[0];
			}
			resultData.put(Constants.SOURCE, source);
		}
		/**
		 * 标准化时间 "2015-12-14 14:38:17 来源: 央广网"
		 */
	if(resultData.containsKey(Constants.POST_TIME))
	{
		String posttime = (String) resultData.get(Constants.POST_TIME);
		
		if (posttime.contains("时间:") && posttime.contains("星期")) {
			posttime = posttime.replace("时间:", "").split("星期")[0].trim();
		}
		/**
		 *  "post_time": "来源：三秦网 时间： 2015-12-21 08:46"
		 */
		else if (posttime.contains("时间：") && posttime.contains("来源：")){
			posttime = posttime.split("时间：").length >1?posttime.split("时间：")[1]:"";
		}
		else if (posttime.contains("日期") && posttime.contains("来源：")){
			Matcher dateMatcher = DATE_PATTERN.matcher(posttime);
			if(dateMatcher.find()) {
				posttime = dateMatcher.group(1);
			} else {
				posttime = "";
			}
		}
		else if(posttime.contains("来源")) {
			if(posttime.trim().split("来源")[0].trim().isEmpty()){
				String[] tempArr = posttime.trim().split(resultData.get(Constants.SOURCE).toString());
				if (tempArr.length > 1) {
					posttime =  tempArr[1].toString();
				} else {
					posttime = "";
				}
			} else {
				posttime = posttime.split("来源")[0].trim();
			}
		}
		else if(posttime.contains("\t来源")) {
			posttime = posttime.split("\t来源")[0].trim();
		}
		resultData.put(Constants.POST_TIME, posttime);
//		LOG.info("url:" + unit.getUrl() + "post_time is " + post_time);
	}
	}
}
