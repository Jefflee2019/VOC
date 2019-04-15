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
 * 站点名：Nnative
 * 
 * 功能：添加评论页下一页以及标准化部分字段
 * 
 * @author bfd_06
 */
public class NnativeCommentRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		if (resultData.containsKey(Constants.COMMENTS)) {
			List<Map<String, Object>> comments = (List<Map<String, Object>>) resultData
					.get(Constants.COMMENTS);
			for (Map<String, Object> comment : comments) {
				if (comment.containsKey(Constants.UP_CNT)) {
					formatAttr(Constants.UP_CNT, comment.get(Constants.UP_CNT)
							.toString(), comment);
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
			}
		}
		/**
		 * 判断是否含有下一页 有则给出
		 */
		String url = unit.getUrl();
		int reply_cnt = Integer.parseInt((String) resultData
				.get(Constants.REPLY_CNT));
		int pageNum = matchPageNum("&page=(\\d+)", url);
		if (pageNum * 10 < reply_cnt) {
			if (pageNum == 1) {
				addNextUrl(url + "&page=2", unit, resultData, result);
			} else {
				addNextUrl(
						url.replace("page=" + pageNum, "page=" + (pageNum + 1)),
						unit, resultData, result);
			}
		}

		return new ReProcessResult(processcode, processdata);
	}

	public void formatAttr(String keyName, String value,
			Map<String, Object> comment) {
		switch (keyName) {
		case Constants.UP_CNT:
		case Constants.CITY:
			value = value.replace("[", "").replace("]", "");
			comment.put(keyName, value);
			break;
		case Constants.COMMENT_TIME:
			value = value.replace("发表", "").trim();
			comment.put(keyName, value);
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

		return 1;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addNextUrl(String nextUrl, ParseUnit unit,
			Map<String, Object> resultData, ParseResult result) {
		Map<String, String> task = new HashMap<String, String>();
		task.put("link", nextUrl);
		task.put("rawlink", nextUrl);
		task.put("linktype", "newscomment");
		resultData.put(Constants.NEXTPAGE, nextUrl);
		List<Map> tasks = (List<Map>) resultData.get("tasks");
		tasks.add(task);
		ParseUtils.getIid(unit, result);
	}

}
