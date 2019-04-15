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

public class NchinanewsContentRe implements ReProcessor {
	private static final Pattern PATTIME = Pattern
			.compile("[0-9]{4}[-年][0-9]{1,2}[-月][0-9]{1,2}[日\\s]*([0-9]{2}:[0-9]{2})*");

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		if (resultData.containsKey(Constants.POST_TIME)) {
			String postTime = (String) resultData.get(Constants.POST_TIME);
			Matcher mch = PATTIME.matcher(postTime);
			if (mch.find()) {
				postTime = mch.group().replace("年", "-").replace("月", "-").replace("日", "");
				resultData.put(Constants.POST_TIME, postTime);
			}
		}
		if (resultData.containsKey(Constants.SOURCE)) {
			String source = (String) resultData.get(Constants.SOURCE);
			int index = source.indexOf("来源：");
			if (index > 0) {
				int endIndex = source.indexOf(" ", index + 3);
				if (endIndex > 0) {
					source = source.substring(index + 3, endIndex);
				} else {
					source = source.substring(index + 3);
				}
				resultData.put(Constants.SOURCE, source.trim());
			} else {
				resultData.put(Constants.SOURCE, "");
			}
		}
		if (resultData.containsKey(Constants.CATE) && resultData.containsKey("cate2")) {
			String cate = resultData.get(Constants.CATE).toString();
			String[] cates = resultData.get("cate2").toString().replace(": ", "").split(">");
			List<String> cateList = new ArrayList<String>();
			cateList.add(cate);
			for (String cate2 : cates) {
				if (!cate2.equals("")) {
					cateList.add(cate2.trim());
				}
			}
			resultData.put(Constants.CATE, cateList);
			resultData.remove("cate2");
		} else if (resultData.containsKey(Constants.CATE)) {
			Object obj = resultData.get(Constants.CATE);
			if (obj instanceof String) {
				String cateStr = (String) obj;
				String[] cates = null;
				if (cateStr.indexOf("→") > 0) {
					cates = cateStr.replace("本页位置：", "").split("→");
				} else if (cateStr.indexOf(">>") > 0) {
					cates = cateStr.replace("本页位置：", "").split(">>");
				}
				List<String> cateList = new ArrayList<>();
				if (cates != null && cates.length != 0) {
					for (String cate : cates) {
						cateList.add(cate.trim());
					}
				}
				resultData.put(Constants.CATE, cateList);
			}
		}
		if (resultData.containsKey(Constants.CONTENT) && resultData.containsKey("content2")) {
			StringBuilder sb = new StringBuilder();
			sb.append(resultData.get("content2")).append(resultData.get("content"));
			resultData.put(Constants.CONTENT, sb.toString());
			resultData.remove("content2");
		}
		String url = unit.getUrl();
		String pageData = unit.getPageData();

		Pattern pattern = Pattern.compile("href=\"(\\S+)\">下一页</a>");
		Matcher matcher = pattern.matcher(pageData);
		List<Map<String, Object>> tasks = null;
		if (matcher.find()) {
			String nextpage = matcher.group(1);
			Map<String, Object> nextMap = new HashMap<>();
			nextMap.put("link", nextpage);
			nextMap.put("rawlink", nextpage);
			nextMap.put("linktype", "newscontent");
			resultData.put(Constants.NEXTPAGE, nextMap);
			if (resultData.containsKey("tasks")) {
				tasks = (List<Map<String, Object>>) resultData.get("tasks");
			} else {
				tasks = new ArrayList<>();
				resultData.put(Constants.TASKS, tasks);
			}
			tasks.add(nextMap);
		}
		pattern = Pattern.compile("(\\d+).shtml");
		matcher = pattern.matcher(url);
		String topicsID = "";
		String clientID = "";
		if (matcher.find()) {
			topicsID = matcher.group(1);
		}
		// 从页面获取client_id
		pattern = Pattern.compile("appid = '(\\w+)',");
		matcher = pattern.matcher(pageData);
		if (matcher.find()) {
			clientID = matcher.group(1);
		}

		if (!topicsID.equals("") && !clientID.equals("")) {
			Map<String, Object> commentTask = new HashMap<>();
			// http://changyan.sohu.com/node/html?client_id=cyqE875ep&topicsid=7595641
			String commUrl = "http://changyan.sohu.com/node/html?client_id=%s&topicsid=%s";
			commUrl = String.format(commUrl, clientID, topicsID);
			commentTask.put(Constants.LINK, commUrl);
			commentTask.put(Constants.RAWLINK, commUrl);
			commentTask.put(Constants.LINKTYPE, "newscomment");
			if (tasks == null) {
				tasks = new ArrayList<>();
				resultData.put(Constants.TASKS, tasks);
			}
			tasks.add(commentTask);
			resultData.put(Constants.COMMENT_URL, commUrl);
			ParseUtils.getIid(unit, result);
		}
		return new ReProcessResult(SUCCESS, processdata);
	}
}
