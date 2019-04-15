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
 * 站点：移动通讯网
 * @author bfd_01
 * 作用：生成下一页任务
 *
 */
public class NmscbscListRe implements ReProcessor {

//	private static final Log LOG = LogFactory.getLog(NmscbscListRe.class);
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();

		if (resultData != null && !resultData.isEmpty()) {
			// nextpage
			List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
			String url = unit.getUrl();
			String nextpage = null;
			nextpage = getNextpage(url);
			if (nextpage != null) {
				Map<String, Object> nextpageTask = new HashMap<String, Object>();
				nextpageTask .put("link", nextpage);
				nextpageTask.put("rawlink", nextpage);
				nextpageTask.put("linktype", "newslist");
				resultData.put("nextpage", nextpage);
				tasks.add(nextpageTask);	// 添加下一页任务
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}
	
	private String getNextpage(String url) {
		// http://www.mscbsc.com/cate.php?catid=1&page=50
		// http://www.mscbsc.com/category-1.html
		String nextpage = null;
		String page = null;
		if (!url.contains("page")) {
			nextpage = "http://www.mscbsc.com/cate.php?catid=1" + "&page=2";
		} else {
			Pattern p = Pattern.compile("page=(\\d+)");
			Matcher m = p.matcher(url);
			while (m.find()) {
				page = m.group(1);
			}
		}
		if (page != null) {
			nextpage = url.split("page=")[0] + "page=" + (Integer.valueOf(page) + 1);
		}
		return nextpage;
	}

}
