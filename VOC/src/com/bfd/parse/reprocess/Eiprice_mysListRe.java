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
 * @ClassName: Eiprice_mysListRe
 * @author: taihua.li
 * @date: 2019年4月10日 上午10:57:44
 * @Description:添加下一页任务
 */
public class Eiprice_mysListRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		/**
		 * 处理下一页 productSize 当前页的商品数
		 */
		if (resultData.containsKey(Constants.ITEMS)) {
			List<Map<String, Object>> items = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
			List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
			int productSize = items.size();
			// 每页60个商品，但是因为穿插广告，有些页不足60个
			if (productSize >= 50) {
				String url = unit.getUrl();
				getNextPage(resultData, url, tasks, productSize);
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	/**
	 * @param resultData
	 * @param url
	 * @param match
	 * @param tasks
	 * @param totalCnt
	 *            商品总数
	 */
	private void getNextPage(Map<String, Object> resultData, String url, List<Map<String, Object>> tasks,
			int productSize) {
		String pageRex = "page=(\\d+)";
		String nextpage = null;
		Matcher nextPageMatch = Pattern.compile(pageRex).matcher(url);
		if (nextPageMatch.find()) {
			int pageno = Integer.parseInt(nextPageMatch.group(1));
			nextpage = url.replace("page=" + pageno, "page=" + (pageno + 1));
		} else {
			nextpage = url.concat("?page=2");
		}
		Map<String, Object> nextpageTask = new HashMap<String, Object>();
		nextpageTask.put(Constants.LINK, nextpage);
		nextpageTask.put(Constants.RAWLINK, nextpage);
		nextpageTask.put(Constants.LINKTYPE, "eclist");
		resultData.put(Constants.NEXTPAGE, nextpage);
		tasks.add(nextpageTask);
		resultData.put(Constants.TASKS, tasks);
	}
}
