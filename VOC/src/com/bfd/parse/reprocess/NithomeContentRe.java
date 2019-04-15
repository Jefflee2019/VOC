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

/**
 * 站点：IT之家
 * 
 * 功能：添加评论页链接
 * 
 * @author bfd_06
 */

public class NithomeContentRe implements ReProcessor {
	private static final Pattern IIDPATTER = Pattern.compile("(\\d+).htm");

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl();
		Matcher match = IIDPATTER.matcher(url);
		/* 为空则返回 */
		if (url == null) {
			return new ReProcessResult(SUCCESS, processdata);
		}
		String subUrl = urlFilter(url);
		if (subUrl.startsWith("wap")) {
			// SOURCE
			if (resultData.containsKey(Constants.SOURCE)) {
				formatAttrW(Constants.SOURCE, (String) resultData.get(Constants.SOURCE), resultData);
			}
			// POST_TIME
			if (resultData.containsKey(Constants.POST_TIME)) {
				formatAttrW(Constants.POST_TIME, (String) resultData.get(Constants.POST_TIME), resultData);
			}
		} else if (subUrl.startsWith("quan")) {
			// REPLY_CNT
			if (resultData.containsKey(Constants.REPLY_CNT)) {
				formatAttrQ(Constants.REPLY_CNT, (String) resultData.get(Constants.REPLY_CNT), resultData);
				/* 添加评论页 */
				int replyCnt = Integer.parseInt(((String) resultData
						.get(Constants.REPLY_CNT)).replace("个回复", "").trim());
				if (replyCnt > 0) {
					Map<String, Object> commentTask = new HashMap<String, Object>();
					String commentUrl = unit.getUrl() + "#comment";
					commentTask.put("link", commentUrl);
					commentTask.put("rawlink", commentUrl);
					commentTask.put("linktype", "newscomment");
					List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
					tasks.add(commentTask);
					resultData.put(Constants.TASKS, tasks);
					resultData.put(Constants.COMMENT_URL, commentUrl);
					ParseUtils.getIid(unit, result);
				}
			}
		}
		/* 给非quan开头的链接添加评论页 */
		if (resultData.containsKey(Constants.REPLY_CNT) && !subUrl.startsWith("quan")) {
			int replyCnt = Integer.parseInt((String) resultData.get(Constants.REPLY_CNT));
			if (replyCnt != 0) {
				Map<String, Object> commentTask = new HashMap<String, Object>();
				String commentUrl = "http://www.ithome.com/ithome/GetAjaxData.aspx?newsID=%s&type=commentpage&page=1&order=false";
				if (match.find()) {
					commentUrl = String.format(commentUrl, match.group(1));
					commentTask.put("link", commentUrl);
					commentTask.put("rawlink", commentUrl);
					commentTask.put("linktype", "newscomment");
					List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
					tasks.add(commentTask);
					resultData.put(Constants.TASKS, tasks);
					resultData.put(Constants.COMMENT_URL, commentUrl);
					ParseUtils.getIid(unit, result);
				}
			}
		}

		return new ReProcessResult(SUCCESS, processdata);
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

	public void formatAttrW(String keyName, String value, Map<String, Object> result) {
		if (keyName.equals(Constants.SOURCE)) {
			String[] strArray1 = value.split(" ");
			result.put(keyName, strArray1[2]);
		} else if (keyName.equals(Constants.POST_TIME)) {
			String[] strArray2 = value.split(" ");
			result.put(keyName, strArray2[0] + " " + strArray2[1]);
		}
	}

	public void formatAttrQ(String keyName, String value, Map<String, Object> result) {
		// switch (keyName) {
		// case Constants.REPLY_CNT:
		// value = value.substring(0, value.lastIndexOf(' '));
		// result.put(keyName, value);
		// break;
		// }
		if (keyName.equals(Constants.REPLY_CNT)) {
			value = value.substring(0, value.lastIndexOf(' '));
			result.put(keyName, value);
		}
	}
}
