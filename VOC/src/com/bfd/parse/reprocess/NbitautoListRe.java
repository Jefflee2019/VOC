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
 * @site:易车网 (Nbitauto)
 * @function 处理下一页链接
 * 
 * @author bfd_02
 *
 */

public class NbitautoListRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NbitautoListRe.class);

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

		// http://www.cheyisou.com/wenzhang/%E5%8D%8E%E4%B8%BA/1.html
		String url = unit.getUrl();
		String pageData = unit.getPageData();
		if (pageData.contains(">下一页<")) {
			getNextpage(resultData, url);
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	private void getNextpage(Map<String, Object> resultData, String url) {
		Matcher match = Pattern.compile("/(\\d+).html").matcher(url);
		String nextpage = null;
		if (match.find()) {
			int pageindex = Integer.parseInt(match.group(1));
			nextpage = url.replace("/" + pageindex+".html", "/" + (pageindex + 1)+".html");
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