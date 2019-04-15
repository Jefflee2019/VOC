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
 * @site:央广网-新闻(Ncnr)
 * @function 处理下一页翻页异常
 * @author bfd_02
 *
 */

public class NcnrListRe implements ReProcessor {
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(NcnrListRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();
		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			return null;
		}

		// cal nextpage
		// 获取页面源码
		String pageData = unit.getPageData();
		// 获取当前页url
		String url = (String) unit.getTaskdata().get("url");
		if (pageData.contains("下一页") && url.contains("?page")) {
			Pattern ptn = Pattern.compile("\\?page=(\\d+)");
			Matcher m = ptn.matcher(url);
			if (m.find()) {
				int p = Integer.parseInt(m.group(1));
				String nextPage = url.replace("?page=" + p, "?page=" + (p + 1));
				resultData.put(Constants.NEXTPAGE, nextPage);
				//task加入下一页任务
				Map<String, Object> nextpageTask = new HashMap<String, Object>();
				nextpageTask.put(Constants.LINK, nextPage);
				nextpageTask.put(Constants.RAWLINK, nextPage);
				nextpageTask.put(Constants.LINKTYPE, "newslist");
				List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
				if (tasks == null) {
					tasks = new ArrayList<Map<String, Object>>();
					resultData.put(Constants.TASKS, tasks);
				}
				tasks.add(nextpageTask);
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
