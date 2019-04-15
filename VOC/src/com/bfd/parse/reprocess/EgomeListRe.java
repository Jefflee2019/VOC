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
 * 站点名：国美在线
 * 
 * 主要功能：生成列表页的下一页的链接
 * 
 * @author bfd_02
 *
 */
public class EgomeListRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(EgomeListRe.class);

	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		/**
		 * @function 拼接下一页链接
		 * @param pagecount
		 *            页码数(模板标定)
		 * @mathod 通过页码数判断是否生成下一页链接
		 */
		if (resultData.containsKey("pagecount")) {
			String url = unit.getUrl();
			String pagecount = resultData.get("pagecount").toString();
			Matcher match = Pattern.compile("(\\d+)\\/(\\d+)").matcher(pagecount);
			if (match.find()) {
				// 当前页码数
				int pageindex = Integer.parseInt(match.group(1));
				// 总页数
				int pageno = Integer.parseInt(match.group(2));
				if (pageno > pageindex) {
					getNextpage(unit, result, resultData, url);
				}
			}
		} else {
			LOG.warn("the pagecount is required");
		}
		resultData.remove("pagecount");
		return new ReProcessResult(SUCCESS, processdata);
	}

	private void getNextpage(ParseUnit unit, ParseResult result, Map<String, Object> resultData, String url) {
		String nextpage = null;
		Map<String, Object> nextpageTask = new HashMap<String, Object>();
		// http://list.gome.com.cn/cat10000070-00-0-48-1-0-0-0-1-15L9-0-0-10-0-0-0-0-0.html?page=3
		Matcher match2 = Pattern.compile("\\?page=(\\d+)").matcher(url);
		// 当前页页码
		int pagesize = 0;
		if (match2.find()) {
			pagesize = Integer.parseInt(match2.group(1));
		}
		// 当前为第一页时
		if (url.contains("?page=")) {
			nextpage = url.replace("?page=" + pagesize, "?page=" + (pagesize + 1));
		}
		nextpageTask.put(Constants.LINK, nextpage);
		nextpageTask.put(Constants.RAWLINK, nextpage);
		nextpageTask.put(Constants.LINKTYPE, "eclist");
		if (resultData != null && !resultData.isEmpty()) {
			resultData.put(Constants.NEXTPAGE, nextpage);
			if (resultData.containsKey(Constants.TASKS)) {
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
				tasks.add(nextpageTask);
				ParseUtils.getIid(unit, result);
			}
		}
	}
}
