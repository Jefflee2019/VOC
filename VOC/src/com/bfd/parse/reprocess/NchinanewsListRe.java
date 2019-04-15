package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：Nchinanews
 * 
 * 主要功能：给出列表页下一页
 * 
 * @author bfd_05
 */
public class NchinanewsListRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		
		String url = unit.getTaskdata().get("url").toString();
		String[] urls = url.split("&start=");
		int pageIndex = 0;
		if(urls.length > 1){
			pageIndex = Integer.valueOf(urls[1]);
		}
		if(resultData.containsKey(Constants.ITEMS)){
			List<Map<String, Object>> items = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
			if(items.size() == 10){
				String nextpage = urls[0] + "&start=" + (pageIndex + 10);
				resultData.put(Constants.NEXTPAGE, nextpage);
				Map<String, Object> nextMap = new HashMap<String, Object>();
				nextMap.put("link", nextpage);
				nextMap.put("rawlink", nextpage);
				nextMap.put("linktype", "newslist");
				List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
				tasks.add(nextMap);
			}
		}
		if(resultData.containsKey(Constants.TASKS)){
			List<Map<String, Object>> tasks = (List<Map<String, Object>>)resultData.get(Constants.TASKS);
			for(int i = 0; i < tasks.size();){
				Map<String, Object> task = tasks.get(i);
				if(task.containsKey("link")){
					String link = task.get("link").toString();
					if(link.contains("shipin")){//视频新闻不要
						tasks.remove(task);
						continue;
					}
				}
				i++;
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}
}
