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
 * 站点名：Nedushi
 * 
 * @author bfd_04
 *
 */
public class NedushiCommentRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		/**
		 * "city": "[上海]", 
		 */
		if (resultData != null && !resultData.isEmpty()) {
			// 评论的顶数量和倒数量
			if (resultData.containsKey(Constants.COMMENTS)) {
				List<Object> commList = (List<Object>)resultData.get(Constants.COMMENTS); 
				if(null != commList && commList.size() > 0) {
					for(Object obj : commList) {
						Map tempMap = (Map) obj;
						if(null != tempMap && tempMap.containsKey(Constants.CITY)) {
							String city = tempMap.get(Constants.CITY).toString();
							city = city.replace("[", "").replace("]", "").trim();
							
							tempMap.put(Constants.CITY, city);
						}
					}
				}
			}			
		}

		return new ReProcessResult(processcode, processdata);
	}
}
