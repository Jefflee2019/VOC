package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site:太平洋电脑网-新闻 (Npconline)
 * @function 新闻评论页后处理插件 处理楼层数和引用回复
 * 
 * @author bfd_02
 *
 */

public class NpconlineCommentRe implements ReProcessor {
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();

		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			return null;
		}

		String pageData = unit.getPageData();
		if (pageData.contains("FAKEMAIN:")) {
			String regex = "FAKEMAIN:(.*)CMTURL:";
			Matcher match = Pattern.compile(regex).matcher(pageData);
			if (match.find()) {
				String jsonData = match.group(1);
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}