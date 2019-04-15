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
 * @site:世界经理人(Nceconline)
 * @function 处理下一页链接
 * 
 * @author bfd_02
 *
 */

public class NceconlineListRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NceconlineListRe.class);

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

		// http://www.ceconline.com/FSSearch.do?keyword=%BB%AA%CE%AA&pageno=01&sortBy=0
		String url = unit.getUrl();
		String pageData = unit.getPageData();
		if (pageData.contains("下一页")) {
			getNextpage(resultData, url);
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	private void getNextpage(Map<String, Object> resultData, String url) {
		Matcher match = Pattern.compile("pageno=(\\d+)&").matcher(url);
		String nextpage = null;
		if (match.find()) {
			String pageindex = match.group(1);
			int pageno = Integer.parseInt(pageindex);
			nextpage = url.replace("pageno="+pageindex,"pageno="+(pageno+1));
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