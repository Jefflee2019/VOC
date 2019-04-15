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
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
/**
 * 站点名：和讯网-新闻
 * <p>
 * 主要功能：生成下一页任务
 * @author bfd_01
 *
 */
public class NhexunListRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NhexunListRe.class);

	@Override
	@SuppressWarnings("unchecked")
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (!resultData.isEmpty()) {
			String url = unit.getUrl();
			String nextpage = null;
			// 生成下一页任务
			if (!url.contains("&page=")) {
				nextpage = url + "&t=0&s=1&f=0&page=2";
			} else {				
				int pageNum = getPageNum(url);
				nextpage = url.split("&page=")[0] + "&page=" + (pageNum+1);
			}
			if (nextpage != null) {
				Map<String, Object> nextpageTask = new HashMap<String, Object>();
				nextpageTask.put("link", nextpage);
				nextpageTask.put("rawlink", nextpage);
				nextpageTask.put("linktype", "newslist");
				LOG.info("url:" + url + "taskdata is "
						+ nextpageTask.get("link")
						+ nextpageTask.get("rawlink")
						+ nextpageTask.get("linktype"));
				if (!resultData.isEmpty()) {
					resultData.put("nextpage", nextpage);
					List<Map<String,Object>> tasks = (List<Map<String,Object>>) resultData.get("tasks");
					tasks.add(nextpageTask);
				}
				// 后处理插件加上iid
				ParseUtils.getIid(unit, result);
			}
		}
		return new ReProcessResult(processcode, processdata);
	}

	/**
	 * 取得当前页码
	 * @param unit
	 * @param result
	 */
	private int getPageNum(String url) {
		Pattern p = Pattern.compile("&page=(\\d+)");
		Matcher m = p.matcher(url);
		while (m.find()) {
			return Integer.valueOf(m.group(1));
		}
		return 1;
	}
}
