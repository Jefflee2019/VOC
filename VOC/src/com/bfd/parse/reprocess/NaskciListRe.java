package com.bfd.parse.reprocess;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NaskciListRe implements ReProcessor {
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();

		Map<String, Object> resultData = result.getParsedata().getData();
//		String pageData = unit.getPageData();
		if ((resultData != null) && (!resultData.isEmpty())) {
			String url = unit.getUrl();
			if (url.contains("so.askci.com")) {
				Map<String, Object> nextpageTask = new HashMap<String, Object>();
				String oldPageNum = getPage(url);
				int pageNum = Integer.valueOf(oldPageNum).intValue();
				String nextpage = null;
				if ((pageNum > 0) || (pageNum == 0)) {
					pageNum = Integer.valueOf(oldPageNum).intValue() + 1;
					nextpage = url.replace("&p=" + oldPageNum, "&p=" + pageNum);
				} else {
					new ReProcessResult(processcode, processdata);
				}
				if (pageNum < 75) {
					nextpageTask.put("link", nextpage);
					nextpageTask.put("rawlink", nextpage);
					nextpageTask.put("linktype", "newslist");
					resultData.put("nextpage", nextpage);
					@SuppressWarnings("unchecked")
					List<Object> tasks = (List<Object>) resultData.get("tasks");
					tasks.add(nextpageTask);
				}
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}

	private String getPage(String url) {
		if (url.contains("so.askci.com")) {
			Pattern iidPatter = Pattern.compile("&p=(\\d+)");
			Matcher match = iidPatter.matcher(url);
			if (match.find()) {
				return match.group(1);
			}
			return "0";
		}
		return "0";
	}
}