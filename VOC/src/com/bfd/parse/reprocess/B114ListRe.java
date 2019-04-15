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
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site:C114 (B114)
 * @function 帖子列表页增加nextpage
 * 
 * @author bfd_04
 *
 */

public class B114ListRe implements ReProcessor {
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(B114ListRe.class);
	private static final Pattern PATTERN = Pattern.compile("p=(\\d+)");
	private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{4}-\\d{1,2}-\\d{1,2})");

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> resultData = result.getParsedata().getData();
		Map<String, Object> processdata = new HashMap<String, Object>();
//		String url = result.getSpiderdata().get("location").toString();
		String pageData = unit.getPageData();
		String pageIndex = "";
		if (resultData != null && !resultData.isEmpty()) {
			/*if(pageData.contains("下一页")) {
				if(!url.contains("p=")) {
					url = url + "&p=1";
				}
				Matcher match = PATTERN.matcher(url);
				if(match.find()){
					pageIndex = match.group(1);
					int nextIndex = Integer.parseInt(pageIndex) + 1;
					url = url.replace("p=" + pageIndex, "p=" + nextIndex);
					Map<String, Object> nextpageTask = new HashMap<String, Object>();
					nextpageTask.put("link", url);
					nextpageTask.put("rawlink", url);
					nextpageTask.put("linktype", "bbspostlist");//任务为列表页
					
					resultData.put("nextpage", url);
					List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
					if(tasks == null){
						tasks = new ArrayList<Map<String, Object>>();
						resultData.put("tasks", tasks);
					}
					tasks.add(nextpageTask);
				} 
			}*/
			if (resultData.containsKey(Constants.ITEMS)) {
				List items = (List)resultData.get(Constants.ITEMS);
				for (int i=0;i<items.size();i++) {
					Map map = (Map)items.get(i);
					// 添加回复数字段
					map.put(Constants.REPLY_CNT, -1024);
					//修改时间
					if(map.containsKey(Constants.POSTTIME)) {
						String posttime = map.get(Constants.POSTTIME).toString();
						Matcher match = DATE_PATTERN.matcher(posttime);
						if(match.find()) {
							posttime = match.group(1);
							map.put(Constants.POSTTIME, posttime);
						} else {
							map.put(Constants.POSTTIME, "");
						}
					}
				}
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
