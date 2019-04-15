package com.bfd.parse.reprocess;

import java.util.ArrayList;
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
 * 站点名：Ninvestide
 * <p>
 * 主要功能：处理生成任务的链接
 * 
 * @author bfd_01
 *
 */
public class NinvestideListRe implements ReProcessor {
	// private static final Log LOG = LogFactory.getLog(N21cnitListRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {

		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			String pagedata = unit.getPageData();
			String url = unit.getUrl();
			// 处理下一页链接
			// http://www.investide.cn/page/26?s=%E5%8D%8E%E4%B8%BA
			if (pagedata.contains("下一页")) {
				String nextpage = null;
				List<Map<String, Object>> tasks = null;
				Map<String, Object> nextpageTask = new HashMap<String, Object>();
				Matcher match = Pattern.compile("page/(\\d+)").matcher(url);
				if (match.find()) {
					int pageId = Integer.parseInt(match.group(1));
					nextpage = url.replace("page/" + pageId, "page/" + (pageId + 1));
				}
				
				if (resultData.containsKey(Constants.TASKS)) {
					tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
				} else {
					tasks = new ArrayList<Map<String, Object>>();
				}
				nextpageTask.put("link", nextpage);
				nextpageTask.put("rawlink", nextpage);
				nextpageTask.put("linktype", "newslist");
				resultData.put("nextpage", nextpage);
				tasks.add(nextpageTask);
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}
}