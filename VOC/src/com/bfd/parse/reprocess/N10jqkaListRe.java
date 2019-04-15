package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

public class N10jqkaListRe implements ReProcessor {

//	private static final Log LOG = LogFactory.getLog(N10jqkaListRe.class);
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			String pagedata = null;
			pagedata = unit.getPageData();
			String url = null;
			url = unit.getUrl();
			// 做url的处理，获得重定向之后的url
			if (resultData.containsKey(Constants.TASKS)) {
				List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData
						.get(Constants.TASKS);
				ConstantFunc.decodeLink(tasks, pagedata);
				// 处理下一页链接
				if (pagedata.contains(">下一页<") && url.contains("www.so.com")) {
					ConstantFunc.getNextpage(resultData, url, tasks);
				}
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}
}
