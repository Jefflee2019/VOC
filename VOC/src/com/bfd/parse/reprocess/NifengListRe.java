package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：21cnit
 * <p>
 * 主要功能：处理生成任务的链接
 * 
 * @author bfd_01
 *
 */
public class NifengListRe implements ReProcessor {
//	private static final Log LOG = LogFactory.getLog(N21cnitListRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {

		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			String pagedata = null;
			pagedata = unit.getPageData();
			String url = null;
			url = unit.getUrl();
			// 做url的处理，获得重定向之后的url
				// 处理下一页链接
			if (pagedata.contains(">下一页 <")) {
				String nextpage = null;
				int pageid = 0;
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
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}
	
}
