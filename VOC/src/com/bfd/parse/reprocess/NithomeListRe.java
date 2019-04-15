package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 站点：IT之家
 * 
 * 功能：解决新闻列表页边界问题 
 * 问题描述：最后一页跳转至第一页
 * 
 * @author bfd_06
 */

public class NithomeListRe implements ReProcessor {
	private static final Pattern IIDPATTER = Pattern.compile("p=(\\d+)");

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Matcher match = IIDPATTER.matcher(unit.getUrl());
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		if (match.find()) {
			int pageNum = Integer.parseInt(match.group(1));
			if (pageNum == 74) {
				List<Map<String, String>> tasks = (List<Map<String, String>>) resultData
						.get("tasks");
				tasks.remove(0);
				resultData.remove("nextpage");
			}
		}

		return new ReProcessResult(SUCCESS, processdata);
	}
	
}
