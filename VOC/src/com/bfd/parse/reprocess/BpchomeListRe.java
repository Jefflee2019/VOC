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
 * 驱动之家论坛列表页
 * 后处理插件
 * 处理下一页
 * @author bfd_05
 *
 */
public class BpchomeListRe implements ReProcessor{

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> resultData = result.getParsedata().getData();
		Map<String, Object> processdata = new HashMap<String, Object>();
		String url = unit.getTaskdata().get("url").toString();
//		String pageData = unit.getPageData();
		Pattern p = Pattern.compile(url + "t" + "\\d+");
		if(resultData.containsKey(Constants.TASKS)){
			List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
			for(int i = 0; i < tasks.size();){
				Map<String, Object> task = tasks.get(i);
				String link = task.get("link").toString();
				Matcher m = p.matcher(link);
				if(m.find()){
					tasks.remove(task);
					continue;
				}
				i++;
			}
		}
		//该网站列表页只能翻50
//		if(!url.contains("page=50") && pageData.contains("下一页")){
//			int pageIndex = 1;
//			String[] urls = url.split("&page=");
//			if(urls.length > 1){
//				pageIndex = Integer.valueOf(urls[1]);
//			}
//			url = urls[0] + "&page=" + (pageIndex + 1);
//			Map<String, Object> nextpageTask = new HashMap<String, Object>();
//			nextpageTask.put("link", url);
//			nextpageTask.put("rawlink", url);
//			nextpageTask.put("linktype", "bbspostlist");//任务为列表页
//			LOG.info("url:" + unit.getUrl() + "taskdata is " + nextpageTask.get("link") + nextpageTask.get("rawlink") + nextpageTask.get("linktype"));
//			if (resultData != null && resultData.size() > 0) {
//				resultData.put("nextpage", url);
//				List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
//				if(tasks == null){
//					tasks = new ArrayList<Map<String, Object>>();
//					resultData.put("tasks", tasks);
//				}
//				tasks.add(nextpageTask);
//				resultData.put("nextpage_", nextpageTask);
//			}
//		}
//		if(resultData.containsKey(Constants.ITEMS)){
//			List<Map<String, Object>> items = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
//			for(Map<String, Object> item : items){
//				item.put(Constants.REPLY_CNT, -1024);
//			}
//		}
		ParseUtils.getIid(unit, result);
//		LOG.info("url:" + unit.getUrl() + ".after reprocess rs is "
//				+ JsonUtil.toJSONString(resultData));
		return new ReProcessResult(SUCCESS, processdata);
	}
}
