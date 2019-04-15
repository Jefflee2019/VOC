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
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site Bhupu
 * @function 帖子列表页后处理
 * @author bfd_02
 * 
 */
public class BhupuListRe implements ReProcessor {

	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(BhupuListRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		if (resultData.containsKey(Constants.ITEMS)) {
			List<Map<String, Object>> items = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
			if (items != null && !items.isEmpty()) {
				for (Map<String, Object> item : items) {

					/**
					 * 0 / 2
					 */
					if (item.containsKey(Constants.REPLY_CNT)) {
						String replyCnt = item.get(Constants.REPLY_CNT).toString();
						if (replyCnt.contains("/")) {
							replyCnt = replyCnt.split("/")[0].trim();
							item.put(Constants.REPLY_CNT, Integer.parseInt(replyCnt));
						}
					}
				}

				// 因为翻页会莫名中断，且只能显示10页，采取直接录入的方式，而不再自动翻页 2018-11-07
				// 论坛是增量抓取，直接录入只有第一页会生效
				// 通过每页的帖子数控制翻页深度,每页40个帖子
				int postcount = items.size();
				if (postcount >= 39) {
					String url = unit.getUrl();
					int pageCnt = 0;
					String nextPage = null;
					// https://bbs.hupu.com/digital
					if (!url.contains("postdate")) {
						Matcher match = Pattern.compile("digital-(\\d+)").matcher(url);
						if (match.find()) {
							pageCnt = Integer.parseInt(match.group(1));
							nextPage = url.replace("digital-" + pageCnt, "digital-" + (pageCnt + 1));
						} else {
							nextPage = url.replace("digital", "digital-2");
						}
					} else {
						Matcher match = Pattern.compile("digital-(postdate)-(\\d+)").matcher(url);
						if (match.find()) {
							pageCnt = Integer.parseInt(match.group(1));
							nextPage = url.replace("digital-postdate-" + pageCnt, "digital-postdate-" + (pageCnt + 1));
						} else {
							nextPage = url.replace("digital-postdate", "digital-postdate-2");
						}
					}
					List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
					Map<String, Object> nextpageTask = new HashMap<String, Object>();
					nextpageTask.put("link", nextPage);
					nextpageTask.put("rawlink", nextPage);
					nextpageTask.put("linktype", "bbspostlist");
					resultData.put("nextpage", nextPage);
					tasks.add(nextpageTask);
				}
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
