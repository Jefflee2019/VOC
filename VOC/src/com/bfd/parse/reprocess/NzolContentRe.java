package com.bfd.parse.reprocess;

import java.util.ArrayList;
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

public class NzolContentRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace paramParserFace) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		List<Map<String, Object>> tasks = null;
		Map<String, Object> resultData = result.getParsedata().getData();

		// 处理发表时间
		if (resultData.containsKey(Constants.POST_TIME)) {
			String posttime = resultData.get(Constants.POST_TIME).toString();
			Pattern pattern = Pattern.compile("[0-9]{4}.[0-9]{1,2}.[0-9]{1,2}(日*\\s*([0-9]{2}:[0-9]{2})*(:[0-9]{2})*)?");
			Matcher m = pattern.matcher(posttime);
			if (m.find()) {
				posttime = m.group();
				resultData.put(Constants.POST_TIME, posttime.trim());
			}
		}

		// 拼接评论页链接
		if (resultData.containsKey("tasks")) {
			tasks = (List<Map<String, Object>>) resultData.get("tasks");
		} else {
			tasks = new ArrayList<Map<String, Object>>();
			resultData.put(Constants.TASKS, tasks);
		}

		String url = unit.getUrl();
		String pageData = unit.getPageData();
		if (pageData.contains("kindid=")) {
			String kindid = getRex("kindid=\\s*(\\d+)", pageData);
			String itemid = getRex("(\\d+).html", url);
			String commUrl = new StringBuffer().append("http://comment.zol.com.cn/").append(kindid).append("/")
					.append(itemid).append("_0_0_1.html").toString();
			resultData.put(Constants.COMMENT_URL, commUrl);
			Map<String, Object> commUrlMap = new HashMap<String, Object>();
			commUrlMap.put("link", commUrl);
			commUrlMap.put("rawlink", commUrl);
			commUrlMap.put("linktype", "newscomment");
			tasks.add(commUrlMap);
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	private String getRex(String rex, String url) {
		String matchData = "";
		Matcher match = Pattern.compile(rex).matcher(url);
		if (match.find()) {
			matchData = match.group(1);
		}
		return matchData;
	}
}
