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
 * 电子产品世界新闻列表页
 * 后处理插件
 * @author bfd_05
 */
public class NeepwListRe implements ReProcessor{
	private static final Log LOG = LogFactory.getLog(NeepwListRe.class);
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> resultData = result.getParsedata().getData();
		Map<String, Object> processdata = new HashMap<String, Object>();
		String pageData = unit.getPageData();
		Pattern p = Pattern.compile("<a class=\"p_redirect\" href=\"(.*)\">&raquo;</a>");
		Matcher mch = p.matcher(pageData);
		if(mch.find()){
			String nextpage = new StringBuilder("http://search.eepw.com.cn").append(mch.group(1)).toString();
			Map<String, Object> nextpageTask = new HashMap<String, Object>();
			nextpageTask.put("link", nextpage);
			nextpageTask.put("rawlink", nextpage);
			nextpageTask.put("linktype", "newslist");//任务为列表页
			LOG.info("url:" + unit.getUrl() + "taskdata is " + nextpageTask.get("link") + nextpageTask.get("rawlink") + nextpageTask.get("linktype"));
			resultData.put("nextpage", nextpage);
			List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
			if(tasks == null){
				tasks = new ArrayList<Map<String, Object>>();
				resultData.put("tasks", tasks);
			}
			tasks.add(nextpageTask);
		}
		ParseUtils.getIid(unit, result);
//		LOG.info("url:" + unit.getUrl() + ".after reprocess rs is "
//				+ JsonUtil.toJSONString(resultData));
		return new ReProcessResult(SUCCESS, processdata);
	}

}
