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
 * @site 网易手机/数码
 * @function 后处理插件
 * @author bfd_05
 *
 */
public class Emobile163ListRe implements ReProcessor {

	private static final Pattern PAGEPATTERN = Pattern.compile("(\\d+).html");

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");

		String url = unit.getUrl();
		List itemList = null;
		if (resultData.containsKey(Constants.ITEMS)) {
			itemList = (List) resultData.get(Constants.ITEMS);
		}
		Matcher mch = PAGEPATTERN.matcher(url);
		int pageIndex = 0;
		if (mch.find() && !itemList.isEmpty()&&itemList.size() >= 9) {
			pageIndex = Integer.valueOf(mch.group(1));
			int nextIndex = pageIndex + 1;
			Map<String, Object> nextpMap = new HashMap<String, Object>();
			String nextpage = url.replace(mch.group(1) + ".html", String.valueOf(nextIndex) + ".html");
			resultData.put(Constants.NEXTPAGE, nextpage);
			nextpMap.put("link", nextpage);
			nextpMap.put("rawlink", nextpage);
			nextpMap.put("linktype", "eclist");
			tasks.add(nextpMap);
		}
		resultData.put("tasks", tasks);
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
