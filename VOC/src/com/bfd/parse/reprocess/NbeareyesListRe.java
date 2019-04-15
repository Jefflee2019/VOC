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

public class NbeareyesListRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl();
		//乱码这个匹配不了
//		String pageData = unit.getPageData();
//		Pattern p = Pattern.compile("范围内共找到 <font color=red><b>(\\d+)</b></font> 个符合要求的结果");
//		Matcher mch = p.matcher(pageData);
		int pageSize = 1;
		int pageIndex = 1;
		if(resultData.containsKey("total_cnt")){
			int totalCnt = Integer.valueOf(resultData.get("total_cnt").toString());
			pageSize = totalCnt%30 == 0 ? totalCnt%30 : totalCnt/30 + 1;
		}
		Pattern p = Pattern.compile("bearpage=(\\d+)");
		Matcher mch = p.matcher(url);
		if(mch.find()){
			pageIndex = Integer.valueOf(mch.group(1));
		} else {
			url = url + "&bearpage=1";
		}
//		http://search.beareyes.com.cn/cgi-bin/bearsee.pl?inputkeyword=%BB%AA%CE%AA&difang=%B1%B1%BE%A9&beartype=all&perpage=30&y=2018|2017&sp=%CB%D1%CE%C4%D5%C2&wap=&bearpage=1
		if(pageIndex < pageSize){
			String sb = url.replaceAll("bearpage=" + pageIndex, "bearpage=" + (pageIndex + 1));
			Map<String, Object> nextMap = new HashMap<String, Object>();
			resultData.put(Constants.NEXTPAGE, sb.toString());
			nextMap.put("link", sb.toString());
			nextMap.put("rawlink", sb.toString());
			nextMap.put("linktype", "newslist");
			List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
			tasks.add(nextMap);
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
