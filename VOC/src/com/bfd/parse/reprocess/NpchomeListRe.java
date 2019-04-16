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
 * 驱动之家新闻列表页
 * 后处理插件
 * 处理下一页
 * @author bfd_05
 *
 */
public class NpchomeListRe implements ReProcessor{

	private static final Log LOG = LogFactory.getLog(NpchomeListRe.class);
	
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> resultData = result.getParsedata().getData();
		Map<String, Object> processdata = new HashMap<String, Object>();
		String url = unit.getTaskdata().get("url").toString();
//		String pageData = unit.getPageData();
//		if(pageData.contains("下一页")){
		int pageIndex = 1;
		String splitStr = "";
		//站内还是搜索引擎
		if(url.contains("search.pchome.net")){
			splitStr = "&page=";
		}
		else if(url.contains("so.com")){
			splitStr = "&pn=";
		}
		String[] urls = url.split(splitStr);
		String tailStr = "";
		if(urls.length > 1){
			String pageStr = urls[1];
			if(urls[1].contains("&")){
				pageStr = urls[1].substring(0, urls[1].indexOf("&"));
				tailStr = urls[1].substring(urls[1].indexOf("&"));
			}
			pageIndex = Integer.valueOf(pageStr);
		}
		String nextpage = urls[0] + splitStr + (pageIndex + 1) + tailStr;
		//TODO 站点有问题，50页之后会返回到第一页，目前暂处理为50页
		if(url.contains("search.pchome.net")){
			if(pageIndex < 50){
				initNextPage(unit, resultData, nextpage);
			}
		}
		else{
			if(pageIndex < 64){
				initNextPage(unit, resultData, nextpage);
			}
		}
		ParseUtils.getIid(unit, result);
//		LOG.info("url:" + unit.getUrl() + ".after reprocess rs is "
//				+ JsonUtil.toJSONString(resultData));
		return new ReProcessResult(SUCCESS, processdata);
	}

	@SuppressWarnings("unchecked")
	private void initNextPage(ParseUnit unit, Map<String, Object> resultData,
			String nextpage) {
		Map<String, Object> nextpageTask = new HashMap<String, Object>();
		nextpageTask.put("link", nextpage);
		nextpageTask.put("rawlink", nextpage);
		nextpageTask.put("linktype", "newslist");//任务为列表页
		LOG.info("url:" + unit.getUrl() + "taskdata is " + nextpageTask.get("link") + nextpageTask.get("rawlink") + nextpageTask.get("linktype"));
		if (resultData != null && !resultData.isEmpty()) {
			resultData.put("nextpage", nextpage);
			List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
			if(tasks == null){
				tasks = new ArrayList<Map<String, Object>>();
				resultData.put("tasks", tasks);
			}
			tasks.add(nextpageTask);
		}
	}
}
