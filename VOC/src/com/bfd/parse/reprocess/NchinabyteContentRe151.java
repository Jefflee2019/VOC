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
 * 站点名：Nchinabyte
 * 
 * 主要功能：给出内容页下一页以及评论页链接 标准化keyword字段
 * 
 * @author bfd_06
 */
public class NchinabyteContentRe151 implements ReProcessor {

	@SuppressWarnings({ "unchecked" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData
				.get(Constants.TASKS);
		String url = unit.getUrl();
		/* 为空则返回 */
		if (url == null) {
			return new ReProcessResult(processcode, processdata);
		}
		/**
		 * 重要备注：代码所使用tmpl_id为151对应ID
		 */
		int tmpl_id = (int) resultData.get("tmpl_id");
		/**
		 * 过滤 http:// https://
		 */
		String subUrl = null;
		if (url.startsWith("http://")) {
			subUrl = url.substring(7);
		} else if (url.startsWith("https://")) {
			subUrl = url.substring(8);
		}
		/**
		 * soft www.yesky 开头的链接
		 */
		if (tmpl_id == 470) {
			String sample = (String) resultData.get(Constants.SOURCE);
			if (subUrl.startsWith("soft.chinabyte.com/hot")) {
				formatData(unit, result, resultData, url);
				return new ReProcessResult(processcode, processdata);
			}
			if (!sample.contains("出处")) {
				resultData.remove(Constants.SOURCE);
			}
			if (!sample.contains("作者")) {
				resultData.remove(Constants.AUTHOR);
			}
			if (resultData.containsKey(Constants.SOURCE)) {
				String value = sample;
				int indexM = value.indexOf("出处");
				int indexN = value.lastIndexOf(' ');
				if (indexN == -1) {
					value = value.substring(indexM + 3);
				} else {
					value = value.substring(indexM + 3, indexN);
				}
				resultData.put(Constants.SOURCE, value);
			}
			if (resultData.containsKey(Constants.AUTHOR)) {
				String value = (String) resultData.get(Constants.AUTHOR);
				value = value.substring(value.indexOf("作者") + 3);
				resultData.put(Constants.AUTHOR, value);
			}
			String postTime = (String) resultData.get(Constants.POST_TIME);
			postTime = postTime.substring(0, 16);
			resultData.put(Constants.POST_TIME, postTime);
			/* 判断如果没有下一页标签则删除下一页链接 */
			if (resultData.containsKey(Constants.NEXTPAGE)
					&& !matchTest(">下一页</a></font>", unit.getPageData())) {
				resultData.remove(Constants.NEXTPAGE);
				tasks.remove(0);
			}
			/**
			 * datacenter net column 开头的链接
			 */
		} else if (tmpl_id == 471) {
			/* 排除net.chinabyte.com下非game域名开头的链接 */
			if (subUrl.startsWith("net")) {
				String subUrl2 = subUrl.replace("net.chinabyte.com/", subUrl);
				if (!subUrl2.startsWith("game")) {
					// KEYWORD
					String keyword = (String) resultData.get(Constants.KEYWORD);
					int index = keyword.indexOf("关键字：");
					keyword = keyword.substring(index + 4);
					resultData.put(Constants.KEYWORD, keyword);
					// 判断内容页是否存在下一页
					if (matchTest("下一页</a>", unit.getPageData())) {
						String urlPageNum = match("/\\d+_(\\d+).shtml", url);
						String nextUrl = url;
						if (urlPageNum == null) {
							nextUrl = nextUrl.replace(".shtml", "_2.shtml");
							addNextContentUrl(resultData, nextUrl);
						} else {
							nextUrl = nextUrl.replace("_" + urlPageNum, "_"
									+ (Integer.parseInt(urlPageNum) + 1));
							addNextContentUrl(resultData, nextUrl);
						}
					}
					// 添加评论页链接
					addCommentUrl(unit, resultData);
					ParseUtils.getIid(unit, result);
					return new ReProcessResult(processcode, processdata);
				}
			}
			// SOURCE
			String source = (String) resultData.get(Constants.SOURCE);
			source = source.substring(3);
			resultData.put(Constants.SOURCE, source);
			// AUTHOR
			String author = (String) resultData.get(Constants.AUTHOR);
			author = author.substring(3);
			resultData.put(Constants.AUTHOR, author);
			// POST_TIME
			String postTime = (String) resultData.get(Constants.POST_TIME);
			postTime = postTime.substring(0, 16);
			resultData.put(Constants.POST_TIME, postTime);
			/**
			 * news it cio 开头的链接
			 */
		} else if (tmpl_id == 284 || tmpl_id == 468 || tmpl_id == 686) {
			formatData(unit, result, resultData, url);
			/**
			 * do 开头的链接
			 */
		} else if (tmpl_id == 681) {
			/* 判断如果没有下一页标签则删除下一页链接 */
			if (!matchTest(">下一页</a></font>", unit.getPageData())
					&& !tasks.isEmpty()) {
				resultData.remove(Constants.NEXTPAGE);
				tasks.remove(0);
			}
		}

		return new ReProcessResult(processcode, processdata);
	}

	public void formatData(ParseUnit unit, ParseResult result,
			Map<String, Object> resultData, String url) {
		// KEYWORD
		String keyword = (String) resultData.get(Constants.KEYWORD);
		int index = keyword.indexOf("关键字：");
		keyword = keyword.substring(index + 4);
		resultData.put(Constants.KEYWORD, keyword);
		// 判断内容页是否存在下一页
		if (matchTest("下一页</a>", unit.getPageData())) {
			String urlPageNum = match("/\\d+_(\\d+).shtml", url);
			String nextUrl = url;
			if (urlPageNum == null) {
				nextUrl = nextUrl.replace(".shtml", "_2.shtml");
				addNextContentUrl(resultData, nextUrl);
			} else {
				nextUrl = nextUrl.replace("_" + urlPageNum,
						"_" + (Integer.parseInt(urlPageNum) + 1));
				addNextContentUrl(resultData, nextUrl);
			}
		}
		// 添加评论页链接
		addCommentUrl(unit, resultData);
		ParseUtils.getIid(unit, result);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addCommentUrl(ParseUnit unit, Map<String, Object> resultData) {
		Map<String, Object> commentTask = new HashMap<String, Object>();
		String urlPageNum = match("/(\\d+)_?\\d*.shtml", unit.getUrl());
		if (urlPageNum != null) {
			StringBuilder commentUrl = new StringBuilder();
			commentUrl
					.append("http://chinabyte.duoshuo.com/api/threads/listPosts.json?thread_key=")
					.append(urlPageNum).append("&limit=50&page=1");
			commentTask.put("link", commentUrl);
			commentTask.put("rawlink", commentUrl);
			commentTask.put("linktype", "newscomment");
			resultData.put("comment_url", commentUrl);
			List<Map> tasks = (List<Map>) resultData.get("tasks");
			tasks.add(commentTask);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addNextContentUrl(Map<String, Object> resultData, String nextUrl) {
		Map<String, Object> commentTask = new HashMap<String, Object>();
		commentTask.put("link", nextUrl);
		commentTask.put("rawlink", nextUrl);
		commentTask.put("linktype", "newscontent");
		resultData.put("nextpage", nextUrl);
		List<Map> tasks = (List<Map>) resultData.get("tasks");
		tasks.add(commentTask);
	}

	public Boolean matchTest(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		return matcher.find();
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
