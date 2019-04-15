package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 手机之家新闻列表页
 * 后处理插件
 * @author bfd_05
 */
public class NimobileListRe implements ReProcessor{
	private static final Log LOG = LogFactory.getLog(NimobileListRe.class);
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> resultData = result.getParsedata().getData();
		Map<String, Object> processdata = new HashMap<String, Object>();
		
		if (unit.getPageData().contains("下一页") && resultData != null
				&& resultData.isEmpty()) {
			String url = unit.getTaskdata().get("url").toString();
			int pageIndex = 1;
			String[] urls = url.split("&page=");
			if(urls.length > 1){
				pageIndex = Integer.valueOf(urls[1]);
			}
			url = urls[0] + "&page=" + (pageIndex + 1);
			
			Map<String, Object> nextpageTask = new HashMap<String, Object>();
			nextpageTask.put("link", url);
			nextpageTask.put("rawlink", url);
			nextpageTask.put("linktype", "newslist");//任务为列表页
			LOG.info("url:" + unit.getUrl() + "taskdata is " + nextpageTask.get("link") + nextpageTask.get("rawlink") + nextpageTask.get("linktype"));
			resultData.put("nextpage", url);
			List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
			if(tasks == null){
				tasks = new ArrayList<Map<String, Object>>();
				resultData.put("tasks", tasks);
			}
			tasks.add(nextpageTask);
			resultData.put("nextpage_", nextpageTask);
		}
		ParseUtils.getIid(unit, result);
//		LOG.info("url:" + unit.getUrl() + ".after reprocess rs is "
//				+ JsonUtil.toJSONString(resultData));
		return new ReProcessResult(SUCCESS, processdata);
	}

}
