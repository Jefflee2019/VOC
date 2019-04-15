package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

public class EzolListRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit,
			ParseResult result, ParserFace face) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData.containsKey(Constants.ITEMS)) {
			List items = (List) resultData.get(Constants.ITEMS);
			List tasks = (List) resultData.get(Constants.TASKS);
			if (items.size() != 0) {
				for (Object object : tasks) {
					Map map = (Map) object; 
					String linktype = (String) map.get("linktype");
					String link = (String) map.get("link");
					if ("eccontent".equals(linktype)) {
						map.put("link", link.concat("?b=b"));
					}
				}
			}
			
		}
		
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
