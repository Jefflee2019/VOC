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

public class BxiciListRe implements ReProcessor {
	private static final Pattern PATTIME = Pattern.compile("[0-9]{4}.[0-9]{1,2}.[0-9]{1,2}");
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace face) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		
		if(resultData.containsKey(Constants.ITEMS)){
			Object obj = resultData.get(Constants.ITEMS);
			if(obj instanceof List){
				List<Map<String, Object>> items = (List<Map<String, Object>>) obj;
				for(Map<String, Object> item : items){
					if(item.containsKey(Constants.POSTTIME)){
						String posttime = item.get(Constants.POSTTIME).toString();
						Matcher mch = PATTIME.matcher(posttime);
						if(mch.find()){
							posttime = mch.group().trim();
							item.put(Constants.POSTTIME, posttime);
						}
						item.put(Constants.REPLY_CNT, -1024);
					}
				}
			}
			
		}
		String url = unit.getUrl();
		//网站75页之后跳回到第一页了
		if(url.contains("p=74")&&resultData.containsKey(Constants.NEXTPAGE)){
			resultData.remove(Constants.NEXTPAGE);
			List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
			for(int i = 0; i < tasks.size();){
				Map<String, Object> task = tasks.get(i);
				if(task.get("linktype").equals("bbspostlist")){
					tasks.remove(task);
					continue;
				}
				i++;
			}
		}
		return new ReProcessResult(SUCCESS, processdata);
	}

}
