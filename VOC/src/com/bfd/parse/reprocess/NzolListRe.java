package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;

public class NzolListRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl();
		List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
		//网站50页之后跳回到第一页了
		if(url.contains("page=50")&&resultData.containsKey(Constants.NEXTPAGE)){
			resultData.remove(Constants.NEXTPAGE);
			for(int i = 0; i < tasks.size();){
				Map<String, Object> task = tasks.get(i);
				if(task.get("linktype").equals("newslist")){
					tasks.remove(task);
					continue;
				}
				i++;
			}
		}
		//剔除不需要的url，包含manu的不是新闻页，还有以zol.com.cn/为结尾的也不是新闻页
		//剔除后降低错误的模板解析
		for(int i = 0; i < tasks.size();){
			Map<String, Object> task = tasks.get(i);
			String link = task.get("link").toString();
			if(link.endsWith("zol.com.cn/") || link.contains("manu_")){
				tasks.remove(task);
				continue;
			}
			i++;
		}
		return new ReProcessResult(SUCCESS, processdata);
	}
} 
