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
 * 站点：Nipadown
 * 功能：列表页后处理-添加下一页
 * @author dph 2017年11月16日
 *
 */
public class NipadownListRe implements ReProcessor{
	private static final Pattern PATTERN_PAGE = Pattern.compile("&p=\\d+");

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> processdata = new HashMap<String, Object>(16);
		Map<String,Object> resultData = result.getParsedata().getData();
		List<Map<String,Object>> items = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
		//添加下一页
		String url = unit.getUrl();
		String nextpage = "&p=2";
		String link = null;
		Matcher pageM = PATTERN_PAGE.matcher(url);
		while(pageM.find()){
			nextpage = pageM.group(0);
			nextpage = nextpage.replace("&p=", "").trim();
			int next = Integer.parseInt(nextpage) + 1;
			//列表页最大100页
			if(100 < next){
				next = 1;
			}
			nextpage = "&p=" + next;
		}
		if(items.size() < 10){
			nextpage = "&p=1";
		}
		url = url.replaceAll("&p=\\d+", "").trim();
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
