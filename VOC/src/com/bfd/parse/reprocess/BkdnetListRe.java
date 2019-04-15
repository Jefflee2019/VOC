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
 * @site:凯迪社区 (Bkdnet)
 * 
 * @function 标准化发表时间 加上回复数
 * 
 * @author bfd_06
 * 
 */

public class BkdnetListRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		// 格式化发表时间 添加回复数-1024
		if (resultData.containsKey(Constants.ITEMS)) {
			List<Map<String, Object>> items = (List<Map<String, Object>>) resultData
					.get(Constants.ITEMS);
			for (Map<String, Object> item : items) {
				if (item.containsKey(Constants.POSTTIME)) {
					String posttime = (String) item.get(Constants.POSTTIME);
					Matcher timeMatcher = Pattern.compile("\\d+-\\d+-\\d+")
							.matcher(posttime);
					if (timeMatcher.find())
						item.put(Constants.POSTTIME, posttime);
				}
				item.put(Constants.REPLY_CNT, -1024);
			}
		}
		
		return new ReProcessResult(SUCCESS, processdata);
	}
}
