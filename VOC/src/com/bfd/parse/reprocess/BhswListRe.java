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
 * 华商论坛 列表页 后处理插件
 * 
 * @author bfd_05
 * 
 */
public class BhswListRe implements ReProcessor {

	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(BhswListRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl();
		
		if (resultData.containsKey(Constants.ITEMS)) {
			List<Map<String, Object>> items = (List<Map<String, Object>>) resultData
					.get(Constants.ITEMS);
			for (Map<String, Object> item : items) {
				if (item.containsKey(Constants.VIEW_CNT)) {
					String replyCountStr = (String) item.get(Constants.VIEW_CNT);
					item.remove(Constants.VIEW_CNT);
					if (replyCountStr.contains("条回复")) {
						String[] replyCounts = replyCountStr.split("条回复");
						int replyCnt = Integer.valueOf(replyCounts[0]);
						item.put(Constants.REPLY_CNT, replyCnt);
					}
				}
				if(item.containsKey(Constants.POSTTIME)){
					String posttime = item.get(Constants.POSTTIME).toString();
					posttime = posttime.substring(0, posttime.length() - 1);
					item.put(Constants.POSTTIME, posttime);
				}
			}
		}
		//列表搜索出来的回复条数，只是大约，改为正则匹配共\d+页
		String pageData = unit.getPageData();
		Pattern p = Pattern.compile("共(\\d+)页");
		Matcher mch = p.matcher(pageData);
		if(mch.find()){
			int totalPage = Integer.valueOf(mch.group(1));
			Matcher indexMch = Pattern.compile("page=(\\d+)").matcher(url);
			int pageIndex = 1;
			if(indexMch.find()){
				pageIndex = Integer.valueOf(indexMch.group(1));
			}
			if(pageIndex  < totalPage) {
				String[] urls = url.split("&page=");
				StringBuilder nextpage = new StringBuilder(urls[0]).append("&page=").append(pageIndex + 1);
				if(url.indexOf("&page=") != -1 & url.indexOf("&", url.indexOf("&page=") + 6) > -1){
					nextpage.append(url.substring( url.indexOf("&page=") + 6));
				}
				Map<String, Object> nextMap =  new HashMap<String, Object>();
				nextMap .put("link", nextpage.toString());
				nextMap.put("rawlink", nextpage.toString());
				nextMap.put("linktype", "bbspostlist");
				List<Map<String, Object>> tasks = null;
				if(resultData.containsKey(Constants.TASKS)){ 
					tasks= (List<Map<String, Object>>) resultData.get(Constants.TASKS);
				}
				else {
					tasks = new ArrayList<Map<String, Object>>();
					resultData.put(Constants.TASKS, tasks);
				}
				resultData.put(Constants.NEXTPAGE, nextpage.toString());
				tasks.add(nextMap);
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
