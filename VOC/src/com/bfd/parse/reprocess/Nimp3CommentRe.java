package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site:随身数码影音-新闻 (Nimp3)
 * @function 评论页后处理插件--评论时间和评论数去噪
 * 
 * @author bfd_02
 *
 */

public class Nimp3CommentRe implements ReProcessor {
	@SuppressWarnings({"unchecked" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();

		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			return null;
		}
		//处理评论数
		if(resultData.containsKey(Constants.REPLY_CNT)) {
			String replyCnt = resultData.get(Constants.REPLY_CNT).toString();
			String cntRegex = "评论(\\d+)条评论";
			replyCnt = getRegex(replyCnt, cntRegex);
			resultData.put(Constants.REPLY_CNT, Integer.parseInt(replyCnt));
		}
		
		//处理评论时间
		if(resultData.containsKey(Constants.COMMENTS)) {}
		List<Map<String,Object>> comments = (List<Map<String,Object>>) resultData.get(Constants.COMMENTS);
		if(!comments.isEmpty()) {
			for(Map<String,Object> commentMap:comments) {
				if(commentMap.containsKey(Constants.COMMENT_TIME)) {
					String commentTime =commentMap.get(Constants.COMMENT_TIME).toString();
					String timeRegex = "([\\d\\.\\s\\:]+)";
					commentTime = getRegex(commentTime,timeRegex);
					commentMap.put(Constants.COMMENT_TIME,commentTime);
				}
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	private String getRegex(String replyCnt, String cntRegex) {
		Matcher match = Pattern.compile(cntRegex).matcher(replyCnt);
		if(match.find()) {
			replyCnt = match.group(1);
		}
		return replyCnt;
	}
}