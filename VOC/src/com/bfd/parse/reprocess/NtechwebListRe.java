package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * techweb新闻列表页 列表页 后处理插件
 * 
 * @author bfd_05
 *
 */
public class NtechwebListRe implements ReProcessor {

	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(NtechwebListRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl();

		// 处理下一页链接
		Map<String, Object> nextpageTask = new HashMap<String, Object>();
		String oldPageNum = getPage(url);
		String nextpage = null;
		if (oldPageNum.equals("0")) {
			nextpage = url + "&start=10";
		} else {
			int pageNum = Integer.valueOf(oldPageNum) + 10;
			nextpage = url.replace("&start=" + oldPageNum, "&start=" + pageNum);
		}

		nextpageTask.put("link", nextpage);
		nextpageTask.put("rawlink", nextpage);
		nextpageTask.put("linktype", "newslist");
		resultData.put("nextpage", nextpage);
		List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
		tasks.add(nextpageTask);
		List<Map<String, Object>> items = (List<Map<String, Object>>) resultData.get("items");
		items.add(nextpageTask);

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	private String getPage(String url) {
		Pattern iidPatter = Pattern.compile("&start=(\\d+)");
		Matcher match = iidPatter.matcher(url);
		if (match.find()) {
			return match.group(1);
		} else {
			return "0";
		}
	}
}