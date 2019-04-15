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
 * 站点：N3533
 * 功能：列表页添加下一页
 * @author dph 2017年11月24日
 *
 */
public class N3533ListRe implements ReProcessor{
	private static final Pattern PATTERN_PAGE = Pattern.compile("&page=\\d+");
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String,Object> resultData = result.getParsedata().getData();
		Map<String, Object> processdata = new HashMap<String, Object>();
		List<Map<String,Object>> items = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
		String url = unit.getUrl();
		int pageNum = 2;
		String page = null;
		Matcher pageMatcher = PATTERN_PAGE.matcher(url);
		if (pageMatcher.find()) {
			page = pageMatcher.group(0);
			page = page.replace("&page=", "");
			pageNum = Integer.parseInt(page) + 1;
		}
		if(null == items || items.size() < 10){
			pageNum = 1;
		}
		//列表页总共为517页
		if(pageNum > 517){
			pageNum = 1;
		}
		String pnNum = "&page=" + pageNum;
		String nextUrl = url.replaceAll("&page=(\\d+)", "").trim() + pnNum;
		Map<String, Object> task = new HashMap<String, Object>();
		task.put("link", nextUrl);
		task.put("rawlink", nextUrl);
		task.put("linktype", "newslist");
		resultData.put("nextpage", nextUrl);
		List<Map> tasks = (List<Map>) resultData.get("tasks");
		tasks.add(task);
		ParseUtils.getIid(unit, result);
	
		return new ReProcessResult(SUCCESS, processdata);
	}

}
