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
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site:手机中国 (Bcnmo)
 * @function 帖子列表页增加nextpage
 * 
 * @author bfd_04
 *
 */

public class BcnmoListRe implements ReProcessor {
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(BcnmoListRe.class);
	private static final Pattern PATTERN = Pattern.compile("-(\\d+).html");

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> resultData = result.getParsedata().getData();
		Map<String, Object> processdata = new HashMap<String, Object>();
		String url = unit.getUrl();
		String pageData = unit.getPageData();
		String pageIndex = "";
		if (resultData != null && !resultData.isEmpty()&& pageData.contains("下一页")) {
			Matcher match = PATTERN.matcher(url);
			if(match.find()){
				pageIndex = match.group(1);
				int nextIndex = Integer.parseInt(pageIndex) + 1;
				url = url.replace("-" + pageIndex + ".html", "-" + nextIndex + ".html");
				Map<String, Object> nextpageTask = new HashMap<String, Object>();
				nextpageTask.put("link", url);
				nextpageTask.put("rawlink", url);
				nextpageTask.put("linktype", "bbspostlist");//任务为列表页
//				LOG.info("url:" + unit.getUrl() + "taskdata is " + nextpageTask.get("link") + nextpageTask.get("rawlink") + nextpageTask.get("linktype"));
				
				resultData.put("nextpage", url);
				List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
				if(tasks == null){
					tasks = new ArrayList<Map<String, Object>>();
					resultData.put("tasks", tasks);
				}
				tasks.add(nextpageTask);
				resultData.put("nextpage_", nextpageTask);
			} 
		}
		ParseUtils.getIid(unit, result);
//		LOG.info("url:" + unit.getUrl() + ".after reprocess rs is "
//				+ JsonUtil.toJSONString(resultData));
//		System.err.println("test result" + JsonUtils.toJSONString(resultData));
		return new ReProcessResult(SUCCESS, processdata);
	}
}
