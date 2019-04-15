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
import com.bfd.parse.reprocess.ReProcessResult;
import com.bfd.parse.reprocess.ReProcessor;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点：N25pp
 * 功能：列表页后处理-添加下一页
 * @author dph 2017年11月17日
 *
 */
public class N25ppListRe implements ReProcessor{

	private static final Pattern PATTERN_PAGE = Pattern.compile("\\d+/");
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> processdata = new HashMap<String, Object>(16);
		Map<String,Object> resultData = result.getParsedata().getData();
		List<Map<String,Object>> items = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
		//添加下一页
		String url = unit.getUrl();
		String nextpage = "2/";
		String link = null;
		Matcher pageM = PATTERN_PAGE.matcher(url);
		while(pageM.find()){
			nextpage = pageM.group(0);
			nextpage = nextpage.replace("/", "").trim();
			int next = Integer.parseInt(nextpage) + 1;
			
			nextpage = next + "/";
		}
		if(items.size() < 6){
			nextpage = "1/";
		}
		url = url.replaceAll("\\d+/", "").trim();
		link = url + nextpage;
		Map<String,String> nextpagetask = new HashMap<String, String>();
		nextpagetask.put("link", link);
		nextpagetask.put("rawlink", link);
		nextpagetask.put("linktype", "newslist");
		resultData.put(Constants.NEXTPAGE, link);
		List<Map> tasks = (List<Map>) resultData.get("tasks");
		tasks.add(nextpagetask);
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
