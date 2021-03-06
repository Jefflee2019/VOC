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
 * @site:环球网-新闻 (Nhuanqiu)
 * @function 新闻列表页后处理插件-处理下一页正常翻页问题
 * 
 * @author bfd_02
 *
 */

public class NtompdaListRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NtompdaListRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();

		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			LOG.warn("未获取到解析数据");
			return null;
		}

		// 获得页面url
		String url = unit.getUrl();
		// 获得页面源码
		String pageData = unit.getPageData();
		if (resultData != null && !resultData.isEmpty() && pageData.contains("下一页")) {

			String nextpage = null;
			int pageid = 0;
			// http://www.tompda.com/list/?cid=7&k=%BB%AA%CE%AA&p=1
			String regex = "&p=(\\d+)";
			Matcher match = Pattern.compile(regex).matcher(url);
			if (match.find()) {
				pageid = Integer.parseInt(match.group(1));
			}
			nextpage = url.replace("&p=" + pageid, "&p=" + (pageid + 1));
			Map<String, Object> nextpageTask = new HashMap<String, Object>();
			nextpageTask.put(Constants.LINK, nextpage);
			nextpageTask.put(Constants.RAWLINK, nextpage);
			nextpageTask.put(Constants.LINKTYPE, "newslist");

			resultData.put(Constants.NEXTPAGE, nextpage);
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
			if (tasks == null) {
				tasks = new ArrayList<Map<String, Object>>();
				resultData.put(Constants.TASKS, tasks);
			}
			tasks.add(nextpageTask);
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}