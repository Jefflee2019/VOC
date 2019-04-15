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
 * 站点名：Neprice
 * 
 * 主要功能： 处理评论及生成下一页任务
 */
public class NepriceCommentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NepriceCommentRe.class);
	private final Pattern dateReg = Pattern.compile("(\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2})");
	private final Pattern floorReg = Pattern.compile("(\\d+)");

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null) {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> comments = (List<Map<String, Object>>) resultData.get("comments");
			if (comments != null && comments.size() > 0) {
				String comment_time;	//评论时间
				String replyfloor;		//回复楼层
				for (Map<String, Object> obj : comments) {
					comment_time = (String) obj.get(Constants.COMMENT_TIME);
					if(comment_time != null){ //处理时间
						Matcher mch = dateReg.matcher(comment_time);
						if(mch.find()){
							obj.put(Constants.COMMENT_TIME, mch.group(1));
						}
					}
					replyfloor = (String) obj.get(Constants.REPLYFLOOR);
					if(replyfloor != null) { //处理回复楼层
						Matcher mch = floorReg.matcher(replyfloor);
						if(mch.find()) {
							obj.put(Constants.REPLYFLOOR, mch.group(1));
						}
					}
				}
			}
			
			String nextPage = (String) resultData.get("next_page");
			if (nextPage != null && nextPage.contains("下一")) {
				// 处理下一页链接
				String url = unit.getUrl();
				String preUrl;
				String oldNum;
				if(url.contains("/rv/")) {
					String temp = url.substring(0, url.lastIndexOf("/rv/"));
					oldNum = temp.substring(temp.lastIndexOf("/")+1);
					preUrl = temp.substring(0, temp.lastIndexOf("/"));
				} else if(url.endsWith("/")) {
					String temp = url.substring(0, url.length()-1);
					oldNum = temp.substring(temp.lastIndexOf("/")+1);
					preUrl = temp.substring(0, temp.lastIndexOf("/"));
				} else {
					oldNum = url.substring(url.lastIndexOf("/")+1);
					preUrl = url.substring(0, url.lastIndexOf("/"));
				}
				try {
					int oldPageNum = Integer.parseInt(oldNum);
					if(oldPageNum == 1) {
						comments.remove(0); // 第一页需要删除第一条作者的评论
					}
					oldPageNum++;
					url = preUrl + "/" + oldPageNum; // nextpage 下一页链接
					Map<String, String> nextpageTask = new HashMap<String, String>();
					nextpageTask.put(Constants.LINK, url);
					nextpageTask.put(Constants.RAWLINK, url);
					nextpageTask.put(Constants.LINKTYPE, "newscomment");
					resultData.put("nextpage", url);
					@SuppressWarnings("unchecked")
					List<Map<String, String>> tasks = (List<Map<String, String>>) resultData.get(Constants.TASKS);
					tasks.add(nextpageTask);
				} catch (NumberFormatException e) {
					LOG.warn("下一页数字解析失败", e);
				}
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}