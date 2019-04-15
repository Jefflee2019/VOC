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

/**
 * 站点名：Ngfan
 * 
 *  生成下一页任务
 * @author bfd_03
 *
 */
public class NgfanListRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		
		List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
		
		String nextpage = null;
		Matcher pageM = Pattern.compile("page=(\\d+)").matcher(unit.getUrl());
		if(pageM.find()){
			String pageNum = pageM.group(1);
			int i = Integer.parseInt(pageNum) + 1;
			if(i <= 50){
				nextpage = unit.getUrl().replaceAll("page=(\\d+)", "page="+i);
				Map<String, Object> nextpageMap = new HashMap<String, Object>();
				nextpageMap.put("link", nextpage);
				nextpageMap.put("rawlink", nextpage);
				nextpageMap.put("linktype", "newslist");
				tasks.add(nextpageMap);
				resultData.put(Constants.TASKS, tasks);
				resultData.put("nextpage", nextpage);
			}
		}
		
		
		
		
		return new ReProcessResult(processcode, processdata);
	}

	/**
	 * 生成下一页
	 * @return 
	 */
	public  String getNextpage(String url){
		Matcher pageM = Pattern.compile("page=(\\d+)").matcher(url);
		if(pageM.find()){
			String pageNum = pageM.group(1);
			int i = Integer.parseInt(pageNum) + 1;
			if(i < 50){
				url = url.replaceAll("page=(\\d+)", "page="+i);
			}
		}
		return url;
	}
}
