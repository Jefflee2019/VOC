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
 * @site:创业邦 (Ncyzone)
 * @function 新闻列表页后处理插件
 * 
 * @author bfd_02
 *
 */

public class NcyzoneListRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NcyzoneListRe.class);

	@SuppressWarnings("unchecked")
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

		if (resultData.containsKey(Constants.ITEMS)) {
			String url = unit.getUrl();
			int itemSize = 0;
			Object items = resultData.get(Constants.ITEMS);
			if (items instanceof List) {
				List<Map<String, Object>> itemList = (List<Map<String, Object>>) items;
				itemSize = itemList.size();
			}
			getNextpageTask(resultData, url, itemSize);
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	private void getNextpageTask(Map<String, Object> resultData, String url, int itemSize) {
		if (itemSize >= 15) {
			String nextpage = null;
			Matcher match = Pattern.compile("&page=(\\d+)").matcher(url);
			if (match.find()) {
				int pageIndex = Integer.parseInt(match.group(1));
				nextpage = url.replace("&page=" + pageIndex, "&page=" + (pageIndex + 1));
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
}