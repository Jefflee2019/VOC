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
 * 站点名：Nali213
 * 
 * 标准化部分字段
 * 
 * @author bfd_06
 */
public class Nali213ContentRe implements ReProcessor {
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		int pageNum = matchPageNum("/\\d+_(\\d+).html", unit.getUrl()); // 当前页码
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		// AUTHOR
		if (resultData.containsKey(Constants.AUTHOR)) {
			formatAttr(Constants.AUTHOR,
					(String) resultData.get(Constants.AUTHOR), resultData);
		}
		// POST_TIME
		if (resultData.containsKey(Constants.POST_TIME)) {
			formatAttr(Constants.POST_TIME,
					(String) resultData.get(Constants.POST_TIME), resultData);
		}
		// BRIEF
		if (resultData.containsKey(Constants.BRIEF)) {
			formatAttr(Constants.BRIEF,
					(String) resultData.get(Constants.BRIEF), resultData);
		}
		// 判断是否含有下一页
		if (haveNextUrl("下一页</a>", unit.getPageData())) {
			if (pageNum == 0)
				addNextUrl(unit.getUrl().replace(".html", "_2.html"),
						resultData, unit, result);
			else
				addNextUrl(
						unit.getUrl().replace(pageNum + ".html",
								pageNum + 1 + ".html"), resultData, unit,
						result);
		}
		/**
		 * 判断是否含有评论数据 有则给出
		 */
		if (resultData.containsKey(Constants.REPLY_CNT)) {
			Object obj = resultData.get(Constants.REPLY_CNT);
			if (obj instanceof Integer) {
				int replyCnt = (int) resultData.get(Constants.REPLY_CNT); // 评论数目
				if ((pageNum == 0 || pageNum == 1) && replyCnt > 0) {
					addCommentUrl(unit, resultData, result);
				}
			} else {
				int replyCnt = Integer.parseInt((String) resultData
						.get(Constants.REPLY_CNT));
				if ((pageNum == 0 || pageNum == 1) && replyCnt > 0) {
					addCommentUrl(unit, resultData, result);
				}
			}
		}

		return new ReProcessResult(processcode, processdata);
	}

	public void formatAttr(String keyName, String value,
			Map<String, Object> result) {
		switch (keyName) {
		case Constants.AUTHOR:
			int index = value.indexOf("编辑");
			value = value.substring(index + 3);
			if (value.contains(" ")) {
				value = value.substring(0, value.indexOf(" "));
			}
			if (value.equals("")) {
				result.remove(keyName);
			} else {
				result.put(keyName, value);
			}
			break;
		case Constants.POST_TIME:
			Pattern patten = Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2}\\s+\\d{1,2}:\\d{1,2}:\\d{1,2}");
			Matcher matcher = patten.matcher(value);
			if(matcher.find())
				result.put(keyName, matcher.group());
			break;
		case Constants.BRIEF:
			value = value.replace("【游侠导读】", "");
			result.put(keyName, value);
			break;
		default:
			break;
		}
	}

	public int matchPageNum(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return Integer.parseInt(matcher.group(1));
		}

		return 0;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addCommentUrl(ParseUnit unit, Map<String, Object> resultData,
			ParseResult result) {
		Map<String, String> commentTask = new HashMap<String, String>();
		/**
		 * 此内容页模板存在多种评论链接类型 将采用依次尝试解析 顺序为：搜狐畅言 ---> 站点常用评论链接格式 ---> 其它评论链接格式
		 */
		String commentUrl = "http://changyan.sohu.com/node/html?client_id=cys15xA6K&topicurl="
				+ unit.getUrl();
		commentTask.put("link", commentUrl);
		commentTask.put("rawlink", commentUrl);
		commentTask.put("linktype", "newscomment");
		resultData.put("comment_url", commentUrl);
		List<Map> tasks = (List<Map>) resultData.get("tasks");
		tasks.add(commentTask);
		ParseUtils.getIid(unit, result);
	}

	public Boolean haveNextUrl(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return true;
		}
		return false;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addNextUrl(String nextUrl, Map<String, Object> resultData,
			ParseUnit unit, ParseResult result) {
		List<Map> tasks = (List<Map>) resultData.get("tasks");
		Map<String, Object> task = new HashMap<String, Object>();
		task.put("link", nextUrl);
		task.put("rawlink", nextUrl);
		task.put("linktype", "newscontent");
		resultData.put("nextpage", nextUrl);
		tasks.add(task);
		ParseUtils.getIid(unit, result);
	}

}
