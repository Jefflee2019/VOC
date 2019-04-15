package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * techweb列表页 后处理插件
 * 
 * @author bfd_05
 * 
 */
public class BtechwebListRe implements ReProcessor {

	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(BtechwebListRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
		String pageData = unit.getPageData();
		if (pageData.contains("下一页</a>")) {
			int pageIndex = 1;
			String url = result.getSpiderdata().get("location").toString();
			String[] urls = url.split("&page=");
			if (urls.length > 1) {
				pageIndex = Integer.valueOf(urls[1]);
			}
			String nextPage = urls[0] + "&page=" + (pageIndex + 1);
			Map<String, Object> nextpageTask = new HashMap<String, Object>();
			nextpageTask.put("link", nextPage);
			nextpageTask.put("rawlink", nextPage);
			nextpageTask.put("linktype", "bbspostlist");// 任务为列表页
			resultData.put("nextpage", nextPage);
			tasks.add(nextpageTask);
		}

		if (resultData.containsKey(Constants.ITEMS)) {
			List<Map<String, Object>> items = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
			for (Map<String, Object> item : items) {
				if (item.containsKey(Constants.REPLY_CNT)) {
					String reply_cnt = item.get(Constants.REPLY_CNT).toString();
					item.put(Constants.REPLY_CNT, reply_cnt.substring(0, reply_cnt.indexOf("个回复")).trim());
					item.put(Constants.VIEW_CNT,
							reply_cnt.substring(reply_cnt.indexOf("-") + 1, reply_cnt.indexOf("次查看")).trim());
				}
				if (item.containsKey(Constants.POSTTIME)) {
					String posttime = ConstantFunc.convertTime(item.get(Constants.POSTTIME).toString().trim());
					item.put(Constants.POSTTIME, posttime);
				}
			}
		}
		// 1 个回复 - 107 次查看
		// LOG.info("url:" + unit.getUrl() + ".after reprocess rs is "
		// + JsonUtil.toJSONString(resultData));
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
