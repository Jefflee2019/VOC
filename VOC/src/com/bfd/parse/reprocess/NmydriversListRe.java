package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 驱动之家新闻列表页
 * 后处理插件
 * @author bfd_05
 */
public class NmydriversListRe implements ReProcessor{
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(NmydriversListRe.class);
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> resultData = result.getParsedata().getData();
		Map<String, Object> processdata = new HashMap<String, Object>();
		String pageData = unit.getPageData();
		if(pageData.contains("下一页</a>")){
			String url = unit.getTaskdata().get("url").toString();
			String[] urls = url.split("&pg=");
			int pageIndex = 1;
			if(urls.length > 1){
				pageIndex = Integer.valueOf(urls[1]);
			}
			String nextpage = urls[0] + "&pg=" + (pageIndex + 1);
			resultData.put(Constants.NEXTPAGE, nextpage);
			List<Map<String, Object>>  tasks = null;
			if(resultData.containsKey("tasks")){
				tasks = (List<Map<String, Object>>) resultData.get("tasks");
			}
			else {
				tasks = new ArrayList<Map<String, Object>>();
				resultData.put("tasks", tasks);
			}
			Map<String, Object> nextMap = new HashMap<String, Object>();
			nextMap.put("link", nextpage);
			nextMap.put("rawlink", nextpage);
			nextMap.put("linktype", "newslist");
			tasks.add(nextMap);		
		}
		
		ParseUtils.getIid(unit, result);
//		LOG.info("url:" + unit.getUrl() + ".after reprocess rs is "
//				+ JsonUtil.toJSONString(resultData));
		return new ReProcessResult(SUCCESS, processdata);
	}

}
