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


public class NiqiyiListRe implements ReProcessor{

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> processdata = new HashMap<String, Object>(16);
		Map<String,Object> resultData = result.getParsedata().getData();
		List<Map<String,Object>> items = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
		List<Map<String,Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
		for (int i = 0; i < items.size(); i++) {
			Map item = items.get(i);
			String posttime = (String) item.get("posttime");
			Map task = tasks.get(i);
			if (item.size() != tasks.size()) {//没有下一页
				task = tasks.get(i + 1);
			}
			String link = (String) task.get("link") + "?post_time=" + posttime;
			String rawlink = (String) task.get("rawlink") + "?post_time=" + posttime;
			task.put("link", link);
			task.put("rawlink", rawlink);
			
		}
		
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
