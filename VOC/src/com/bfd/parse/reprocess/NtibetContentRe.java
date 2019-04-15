package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site：今日头条
 * @function：新闻内容页后处理
 * @author bfd_04
 *
 */
public class NtibetContentRe implements ReProcessor {
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		Map<String, Object> resultData = new HashMap<String, Object>();
		Map<String, Object> processdata = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
		String pageData = unit.getPageData();
		if (resultData != null && !resultData.isEmpty()) {
			if (resultData.containsKey(Constants.POST_TIME)) {
				resultData.put(Constants.POST_TIME,
						ConstantFunc.getDate((String)resultData.get(Constants.POST_TIME)));
			}
			if (resultData.containsKey(Constants.SOURCE)) {
				resultData.put(Constants.SOURCE,
						ConstantFunc.getSource((String)resultData.get(Constants.SOURCE)));
			}
		}
		return new ReProcessResult(SUCCESS, processdata);
	}
}
