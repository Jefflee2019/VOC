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

/**
 * 站点名：E9ji
 * 
 * 功能：添加下一页
 * 
 * @author bfd_06
 */
public class E9jiListRe implements ReProcessor {
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		int pageNumNow = 0;
		int pageNumTotal = 0;
		String urlKeyWord = "";
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl();
		Matcher matcher1 = Pattern.compile("\\?w=([^&]+)").matcher(url);
		Matcher matcher2 = Pattern.compile("page=(\\d+)").matcher(url);
		Matcher matcher3 = Pattern.compile("共(\\d+)条记录").matcher(
				unit.getPageData());
		if (matcher2.find())
			pageNumNow = Integer.parseInt(matcher2.group(1));
		if (!matcher1.find() || !matcher3.find())
			return new ReProcessResult(processcode, processdata);
		urlKeyWord = matcher1.group(1);
		pageNumTotal = Integer.parseInt(matcher3.group(1));

		if (pageNumNow * 28 < pageNumTotal)
			addNextUrl(url, pageNumNow, urlKeyWord, resultData, unit, result);

		return new ReProcessResult(processcode, processdata);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addNextUrl(String url, int pageNumNow, String urlKeyWord,
			Map<String, Object> resultData, ParseUnit unit, ParseResult result) {
		List<Map> tasks = (List<Map>) resultData.get("tasks");
		Map<String, Object> task = new HashMap<String, Object>();
		String nextUrl = "";
		if (pageNumNow == 0)
			nextUrl = "http://www.9ji.com/search?w="
					+ urlKeyWord
					+ "&page=2&cid=0&brandId=0&prices=&navs=&ciName=&brandName&order=0,0";
		else
			nextUrl = url.replace("&page=" + pageNumNow, "&page="
					+ (pageNumNow + 1));
		task.put("link", nextUrl);
		task.put("rawlink", nextUrl);
		task.put("linktype", "eclist");
		resultData.put("nextpage", nextUrl);
		tasks.add(task);
		ParseUtils.getIid(unit, result);
	}

}
