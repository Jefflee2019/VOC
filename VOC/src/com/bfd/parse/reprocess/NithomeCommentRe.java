package com.bfd.parse.reprocess;

import java.util.ArrayList;
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
 * 站点：IT之家
 * 
 * 功能：标准化评论页 给出评论页下一页
 * 
 * @author bfd_06
 */

public class NithomeCommentRe implements ReProcessor {

	private static final Pattern IIDPATTER = Pattern
			.compile("newsID=(\\d+)&type=commentpage&page=(\\d+)");
	private static final Pattern DATAPATTERN = Pattern.compile("(\\d+)");

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String subUrl = urlFilter(unit.getUrl()); // 去除 http https 剩余部分
		if (subUrl.startsWith("www")) {
			List<Map<String, Object>> comments = (List<Map<String, Object>>) resultData
					.get("comments");
			Map<String, Object> firstCom = comments.get(0);
			String firstFloor = firstCom.get(Constants.REPLYFLOOR).toString();
			firstFloor = firstFloor.replace("楼", "");
			if (resultData.containsKey("subcomments")) {
				List<Map<String, Object>> subcomments = (List<Map<String, Object>>) resultData
						.get("subcomments");
				comments.addAll(subcomments);
				resultData.remove("subcomments");
			}
			for (Map<String, Object> comment : comments) {
				if (comment.containsKey(Constants.UP_CNT)) {
					formatAttr(Constants.UP_CNT, comment.get(Constants.UP_CNT)
							.toString(), comment);
				}
				if (comment.containsKey(Constants.DOWN_CNT)) {
					formatAttr(Constants.DOWN_CNT,
							comment.get(Constants.DOWN_CNT).toString(), comment);
				}
				if (comment.containsKey(Constants.COMMENT_TIME)) {
					formatAttr(Constants.COMMENT_TIME,
							comment.get(Constants.COMMENT_TIME).toString(),
							comment);
				}
				if (comment.containsKey(Constants.CITY)) {
					formatAttr(Constants.CITY, comment.get(Constants.CITY)
							.toString(), comment);
				}
				/*删除楼层数 新闻是不需要楼层数的*/
				if (comment.containsKey(Constants.REPLYFLOOR)) {
//					String floor = comment.get(Constants.REPLYFLOOR).toString();
//					Matcher floorMatcher = DATAPATTERN.matcher(floor);
//					if (floorMatcher.find()) {
//						floor = floorMatcher.group(1);
//					}
//					formatAttr(Constants.REPLYFLOOR, floor, comment);
					comment.remove(Constants.REPLYFLOOR);
				}
			}
			/**
			 * 添加下一页
			 */
			if (Integer.parseInt(firstFloor) > 50) {
				Map<String, Object> rtask = new HashMap<String, Object>();
				Matcher match = IIDPATTER.matcher(unit.getUrl());
				String nextUrl = "http://www.ithome.com/ithome/GetAjaxData.aspx?newsID=%s&type=commentpage&page=%s&order=false";
				if (match.find()) {
					nextUrl = String.format(nextUrl,
							Integer.parseInt(match.group(1)),
							Integer.parseInt(match.group(2)) + 1);
					rtask.put("link", nextUrl);
					rtask.put("rawlink", nextUrl);
					rtask.put("linktype", "newscomment");
					resultData.put(Constants.NEXTPAGE, nextUrl);
					List<Map> rtasks = (List<Map>) resultData
							.get(Constants.TASKS);
					if (rtasks == null) {
						rtasks = new ArrayList<Map>();
						resultData.put(Constants.TASKS, rtasks);
					}
					rtasks.add(rtask);
				}
			}
		} else if (subUrl.startsWith("quan")) {
			List<Map<String, Object>> comments = (List<Map<String, Object>>) resultData
					.get("comments");
			for (Map<String, Object> comment : comments) {
				formatAttrQ(Constants.UP_CNT, comment.get(Constants.UP_CNT)
						.toString(), comment);
				formatAttrQ(Constants.DOWN_CNT, comment.get(Constants.DOWN_CNT)
						.toString(), comment);
				if (comment.containsKey(Constants.REPLYFLOOR)) {
					String floor = comment.get(Constants.REPLYFLOOR).toString();
					Matcher floorMatcher = DATAPATTERN.matcher(floor);
					if (floorMatcher.find()) {
						floor = floorMatcher.group(0);
					} else {
						floor = "0";
					}
					resultData.put(Constants.REPLYFLOOR,
							Integer.parseInt(floor));
				}
			}

		}

		// LOG.info("url:" + unit.getUrl() + " The result is "
		// + JsonUtil.toJSONString(resultData));
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	public void formatAttr(String keyName, String value,
			Map<String, Object> comment) {
		switch (keyName) {
		case Constants.UP_CNT:
			value = value.substring(value.indexOf('(') + 1, value.indexOf(')'));
			comment.put(keyName, value);
			break;
		case Constants.DOWN_CNT:
			value = value.substring(value.indexOf('(') + 1, value.indexOf(')'));
			comment.put(keyName, value);
			break;
		case Constants.COMMENT_TIME:
			String time2 = value.substring(value.lastIndexOf(' ') + 1);
			value = value.substring(0, value.lastIndexOf(' '));
			String time1 = value.substring(value.lastIndexOf(' ') + 1);
			comment.put(keyName, time1 + " " + time2);
			break;
		case Constants.CITY:
			value = value.substring(0, value.lastIndexOf(' '));
			value = value.substring(0, value.lastIndexOf(' '));
			comment.put(keyName, value);
			break;
		case Constants.REPLYFLOOR:
			comment.put(keyName, Integer.parseInt(value));
			break;
		default:
			break;
		}
	}

	public void formatAttrQ(String keyName, String value,
			Map<String, Object> comment) {
		value = value.substring(value.indexOf('用') + 2);
		comment.put(keyName, value);
	}

	/**
	 * 过滤 http:// https://
	 */
	public String urlFilter(String url) {
		if (url.startsWith("http://")) {
			return url.substring(7);
		} else {
			return url.substring(8);
		}
	}

}
