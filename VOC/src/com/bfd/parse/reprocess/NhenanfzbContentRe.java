package com.bfd.parse.reprocess;

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
import com.bfd.parse.util.ParseUtils;
/**
 * @site：Nhenanfzb
 * @function 主要功能：处理author字段,评论链接
 * @author bfd_04
 *
 */
public class NhenanfzbContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NhenanfzbContentRe.class);
	private static final Pattern NEWS_ID_PATTERN = Pattern.compile("(\\d+).html");
	private static final Pattern VIEW_CNT_PATTERN = Pattern.compile("(\\d+)");
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			/**
			 * "责任编辑：10049"
			 */
			if (resultData.containsKey(Constants.AUTHOR)) {
				String author = resultData.get(Constants.AUTHOR).toString();
				author = author.replace("责任编辑：", "").trim();
				resultData.put(Constants.AUTHOR, author);
			}
			/**
			 * "cate": [
                "平安河南首页", 
                "", 
                "首页", 
                "法制播报", 
                "通信", 
                "正文"
        ], 
			 */
			if (resultData.containsKey(Constants.CATE)) {
				List cate = (List)resultData.get(Constants.CATE);
				for(int i=0 ;i < cate.size(); i++) {
					String temp = (String) cate.get(i);
					if (temp.equals("")) {
						cate.remove(i);
					}
				}
			}
			/**
			 * "时间：2014-06-25 14:11:41 来源： 浏览次数：1"
			 */
			if(resultData.containsKey(Constants.SOURCE)) {
				String source = resultData.get(Constants.SOURCE).toString();
				if(source.contains("来源：")) {
					source = source.split("来源：").length > 1? source.split("来源：")[1]:"".toString();
					if(!source.equals("")) {
						source = source.split("浏览次数")[0].trim();
						LOG.debug("source: " + source);
					}
				}
				resultData.put(Constants.SOURCE, source);
			}
			/**
			 * "时间：2014-06-25 14:11:41 来源： 浏览次数：1"
			 */
			if(resultData.containsKey(Constants.VIEW_CNT)) {
				String viewCnt = resultData.get(Constants.VIEW_CNT).toString();
				if(viewCnt.contains("浏览次数")) {
					viewCnt = viewCnt.split("浏览次数").length > 1? viewCnt.split("浏览次数")[1]:"";
					Matcher viewMatch = VIEW_CNT_PATTERN.matcher(viewCnt);
					if(viewMatch.find()) {
						viewCnt = viewMatch.group(1);
					} else {
						viewCnt = "";
					}
				}
				resultData.put(Constants.VIEW_CNT, viewCnt);
			}
			/**
			 * "时间：2014-06-25 14:11:41 来源： 浏览次数：1"
			 */
			if(resultData.containsKey(Constants.POST_TIME)) {
				String postTime = resultData.get(Constants.POST_TIME).toString();
				if(postTime.contains("时间：")) {
					postTime = postTime.split("时间：").length > 1? postTime.split("时间：")[1]:"".toString();
					if(!postTime.equals("")) {
						postTime = postTime.split("来源")[0].trim();
					}
				}
				resultData.put(Constants.POST_TIME, postTime);
			}
			
			// 评论链接
			String url = unit.getUrl();
		
			Matcher mch = NEWS_ID_PATTERN.matcher(url);
			if(mch.find()) {
				Map<String, Object> commentTask = new HashMap<String, Object>();
				String urlHead = "http://www.hnfzb.com/reply?newsid=";
				String commUrl = urlHead + mch.group(1);
				commentTask.put("link", commUrl);
				commentTask.put("rawlink", commUrl);
				commentTask.put("linktype", "newscomment");
				if (resultData != null && !resultData.isEmpty()) {
					resultData.put(Constants.COMMENT_URL, commUrl);
					List<Map> tasks = (List<Map>) resultData.get("tasks");
					tasks.add(commentTask);
				}
			}
		}
		// 后处理插件加上iid
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}
}
