package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：Nyidianzixun
 * 
 * 标准化部分字段
 * 
 * @author bfd_06
 */
public class NyidianzixunContentRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		// 宅男爱搞机 2017.4.29 我要分享
		// POST_TIME
		if (resultData.containsKey(Constants.POST_TIME)) {
			String postTime = resultData.get(Constants.POST_TIME).toString();
			resultData.put(Constants.POST_TIME, ConstantFunc.convertTime(postTime));
		}

		/**
		 * 添加评论页链接
		 * http://www.yidianzixun.com/home/q/getcomments?docid=0GG0sT9P&count=30
		 */
		addCommentUrl(unit, result, resultData);
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}

	@SuppressWarnings("unchecked")
	private void addCommentUrl(ParseUnit unit, ParseResult result, Map<String, Object> resultData) {
		String url = unit.getUrl();
		Matcher match = Pattern.compile("/article/(\\S*)").matcher(url);
		StringBuffer sb = new StringBuffer();
		if (match.find()) {
			String docid = match.group(1);
			Map<String, Object> commentTask = new HashMap<String, Object>();
			String commentUrl = sb.append("http://www.yidianzixun.com/home/q/getcomments?docid=").append(docid)
					.append("&count=30").toString();
			commentTask.put("link", commentUrl);
			commentTask.put("rawlink", commentUrl);
			commentTask.put("linktype", "newscomment");
			resultData.put("comment_url", commentUrl);
			List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
			tasks.add(commentTask);
		}
	}
}
