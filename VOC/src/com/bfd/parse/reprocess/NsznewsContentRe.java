package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * @site：深圳新闻网
 * @function：新闻内容页后处理
 * @author huangzecheng 2016-11-28
 *
 */
public class NsznewsContentRe implements ReProcessor {
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		Map<String, Object> resultData = new HashMap<String, Object>();
		Map<String, Object> processdata = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
//		String pageData = unit.getPageData();
		if (resultData != null && !resultData.isEmpty()) {
			if (resultData.containsKey(Constants.POST_TIME)) {
				resultData.put(Constants.POST_TIME,
						ConstantFunc.getDate((String)resultData.get(Constants.POST_TIME)));
			}
			if (resultData.containsKey(Constants.SOURCE)) {
				String source = (String)resultData.get(Constants.SOURCE);
				int indx = source.indexOf("来源：");
				if(indx >= 0) {
					String[] s = source.substring(indx+3).trim().split(" ");
					resultData.put(Constants.SOURCE, s[0]);
				}
			}
		}
		return new ReProcessResult(SUCCESS, processdata);
	}
}
