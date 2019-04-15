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
 * 站点名：Bxcar
 * 
 * 功能：标准化回复数 解决边界问题
 * 
 * @author bfd_06
 */
public class BxcarListRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl();
		// ITEMS
		if (resultData.containsKey(Constants.ITEMS)) {
			List<Map<String, Object>> items = (List<Map<String, Object>>) resultData
					.get(Constants.ITEMS);
			for (Map<String, Object> item : items) {
				String replyCnt = (String) item.get(Constants.REPLY_CNT);
				Matcher matcher = Pattern.compile("\\(?(\\d+)\\)?").matcher(replyCnt);
				if (matcher.find()) {
					item.put(Constants.REPLY_CNT, matcher.group(1));
				} else {
					item.put(Constants.REPLY_CNT, "0");
				}
			}
		}

		if (matchTest("class=\"page_down\">下一页</a>", unit.getPageData())) {
			// 删除最后一页的下一页
			String pageNumStr = match("pn=(\\d+)", url);
			if (pageNumStr != null) {
				String nextUrl = url.replace(
						"&pn=" + Integer.parseInt(pageNumStr), "&pn="
								+ (Integer.parseInt(pageNumStr) + 1));
				Map<String, Object> rtask = new HashMap<String, Object>();
				rtask.put("link", nextUrl);
				rtask.put("rawlink", nextUrl);
				rtask.put("linktype", "bbspostlist");
				resultData.put("nextpage", nextUrl);
				List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData
						.get("tasks");
				tasks.add(rtask);
				ParseUtils.getIid(unit, result);
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

	public Boolean matchTest(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return true;
		}

		return false;
	}

}
