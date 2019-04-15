package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
import com.bfd.parse.entity.Constants;

/**
 * 站点名：Nautohome
 * 
 * 功能：处理边界问题 给出评论页连接
 * 
 * @author bfd_06
 */
public class NautohomeContentRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		List<Map<String, Object>> rtasks = (List<Map<String, Object>>) resultData
				.get(Constants.TASKS);

		/**
		 * 给内容页第一页加上评论页链接
		 */
		String pageNumStr = match("-(\\d+).html", unit.getUrl());
		if (pageNumStr == null || pageNumStr.equals("1")) {
			Map<String, Object> rtask = new HashMap<String, Object>();
			String pageId = match("(\\d+).html", unit.getUrl());
			if (pageId != null) {
				String commentUrl = "http://reply.autohome.com.cn/api/comments/show.json?count=50&page=1&id=t_id&appid=1&datatype=jsonp&order=0&replyid=0".replace("t_id", pageId);
				rtask.put("link", commentUrl);
				rtask.put("rawlink", commentUrl);
				rtask.put("linktype", "newscomment");
				rtasks.add(rtask);
				resultData.put(Constants.COMMENT_URL, commentUrl);
				ParseUtils.getIid(unit, result);
			}
		}

		/**
		 * 删掉下一页为空的最后一页
		 */
		if (resultData.containsKey(Constants.NEXTPAGE)) {
			/**
			 * 此处注意 系统中取出的nextpage为String 测试时为Map<String, Object>
			 */
//			Map<String, Object> nextpage = (Map<String, Object>) resultData
//					.get(Constants.NEXTPAGE);
//			if (nextpage.get(Constants.LINK) == "") {
//				List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData
//						.get(Constants.TASKS);
//				tasks.remove(0);
//				resultData.remove(Constants.NEXTPAGE);
//			}
			String nextpage = (String) resultData
					.get(Constants.NEXTPAGE);
			if (nextpage.equals("")) {
				List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData
						.get(Constants.TASKS);
				tasks.remove(0);
				resultData.remove(Constants.NEXTPAGE);
			}
		}

		return new ReProcessResult(processcode, processdata);
	}

	public String match(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}
}
