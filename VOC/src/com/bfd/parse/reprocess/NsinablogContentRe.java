package com.bfd.parse.reprocess;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NsinablogContentRe implements ReProcessor {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map processdata = new HashMap();
		Map resultData = result.getParsedata().getData();
		if (resultData.containsKey("tag"))
			formatAttr("tag", (String) resultData.get("tag"), resultData);
		if (resultData.containsKey("sendGoldPen_cnt"))
			formatAttr("sendGoldPen_cnt",
					(String) resultData.get("sendGoldPen_cnt"), resultData);
		if (resultData.containsKey("view_cnt"))
			formatAttr("view_cnt", (String) resultData.get("view_cnt"),
					resultData);
		if (resultData.containsKey("post_time"))
			formatAttr("post_time", (String) resultData.get("post_time"),
					resultData);
		if (resultData.containsKey("content"))
			formatAttr("content", (String) resultData.get("content"),
					resultData);
		if (resultData.containsKey("reply_cnt")) {
			int replyCnt = ((Integer) resultData.get("reply_cnt")).intValue();
			if (replyCnt > 0)
				addCommentUrl(unit, resultData, result);
		}
		
		return new ReProcessResult(processcode, processdata);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void formatAttr(String keyName, String value, Map result) {
		String s;
		switch ((s = keyName).hashCode()) {
		default:
			break;

		case -1973195749:
			if (s.equals("sendGoldPen_cnt")) {
				value = value.replace("\u652F", "");
				result.put(keyName, value);
			}
			break;

		case 114586:
			if (s.equals("tag")) {
				value = value.replace("\u6807\u7B7E\uFF1A", "").trim();
				result.put(keyName, value);
			}
			break;

		case 951530617:
			if (!s.equals("content"))
				break;
			if (value.startsWith("\u6458\u8981\uFF1A"))
				value = value.substring(3);
			result.put(keyName, value);
			break;

		case 1196167375:
			if (s.equals("view_cnt")) {
				value = value.replace(",", "");
				result.put(keyName, value);
			}
			break;

		case 2002966284:
			if (s.equals("post_time")) {
				value = value.replace("(", "").replace(")", "");
				result.put(keyName, value);
			}
			break;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addCommentUrl(ParseUnit unit, Map resultData, ParseResult result) {
		String url = unit.getUrl();
		Map commentTask = new HashMap();
		Pattern patten = Pattern.compile("([a-zA-Z0-9]+).html");
		Matcher matcher = patten.matcher(url);
		if (matcher.find()) {
			String urlID = matcher.group(1);
			String commentUrl = (new StringBuilder(
					"http://blog.sina.com.cn/s/comment_")).append(urlID)
					.append("_1.html?comment_v=articlenew").toString();
			commentTask.put("link", commentUrl);
			commentTask.put("rawlink", commentUrl);
			commentTask.put("linktype", "newscomment");
			resultData.put("comment_url", commentUrl);
			List tasks = (List) resultData.get("tasks");
			tasks.add(commentTask);
			ParseUtils.getIid(unit, result);
		}
	}

}
