package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site:万维家电网 (Nea3w)
 * @function 处理下一页链接
 * 
 * @author bfd_02
 *
 */

public class Nea3wListRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(Nea3wListRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();

		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			LOG.warn("未找到解析数据");
			return null;
		}

		/**
		 * @param nextpage
		 * @function 拼接下一页任务
		 */

		// http://search.ea3w.com/?action=Doc&keyword=%BB%AA%CE%AA
		// http://search.ea3w.com/?action=Doc&keyword=%BB%AA%CE%AA&page=1
		String url = unit.getUrl();
		String pageData = unit.getPageData();
		if (pageData.contains(">下一页 >")) {
			getNextpage(resultData, url);
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	private void getNextpage(Map<String, Object> resultData, String url) {
		Matcher match = Pattern.compile("&page=(\\d+)").matcher(url);
		String nextpage = null;
		if (match.find()) {
			int pageindex = Integer.parseInt(match.group(1));
			nextpage = url.replace("&page=" + pageindex, "&page=" + (pageindex + 1));
		} else {
			nextpage = url.concat("&page=2");
		}

		resultData.put(Constants.NEXTPAGE, nextpage);

		Map<String, Object> nextpageTask = new HashMap<String, Object>();
		nextpageTask.put("link", nextpage);
		nextpageTask.put("rawlink", nextpage);
		nextpageTask.put("linktype", "newslist");

		List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
		tasks.add(nextpageTask);
		resultData.put(Constants.TASKS, tasks);
	}
}