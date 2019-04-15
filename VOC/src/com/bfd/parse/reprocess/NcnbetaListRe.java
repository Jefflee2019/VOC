package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

public class NcnbetaListRe implements ReProcessor{

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
		
		List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
		resultData.put(Constants.TASKS, tasks);
		if(resultData.containsKey(Constants.NEXTPAGE)){
			Object obj = resultData.get(Constants.NEXTPAGE);
			if(obj instanceof Map){
				tasks.add((Map<String, Object>)obj);
			}
			else if(obj instanceof String){
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("link", obj.toString());
				m.put("rawlink", obj.toString());
				m.put("linktype", "newslist");
				tasks.add(m);
			}
		}
		if(resultData.containsKey(Constants.ITEMS)){
			List<Map<String, Object>> items = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
			for(Map<String, Object> item : items){
				if(item.containsKey(Constants.POSTTIME) && item.containsKey(Constants.LINK)){
					String posttime = item.get(Constants.POSTTIME).toString();
					String[] pts = posttime.split(" ");
					if(pts.length > 1){
						int compare = pts[1].compareTo("2013-1-1");
						if(compare >= 0){
							Object linkObj = item.get(Constants.LINK);
							if(linkObj instanceof Map){
								Map<String, Object> linkMap = (Map<String, Object>) linkObj;
								String link = linkMap.get(Constants.LINK).toString();
								if(link.contains("articles")){
									tasks.add(linkMap);
								}
							}
						}
					}
				}
			}
		}	
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
