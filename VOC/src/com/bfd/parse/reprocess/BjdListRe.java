package com.bfd.parse.reprocess;

import java.util.ArrayList;
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
 * 站点名：京东(论坛)
 * 
 * 主要功能：
 * 		格式化用户数，帖子数
 * 		生成列表页的下一页的链接
 * 
 * @author bfd_03
 *
 */
public class BjdListRe implements ReProcessor {
	Pattern pattern = Pattern.compile("(http://group.jd.com/thread/\\d+/\\d+/)(\\d+)?\\.htm");
	Matcher matcher = null;

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		// 用户数 帖子数
		if (resultData.containsKey(Constants.USER_CNT) && resultData.containsKey(Constants.POST_CNT)) {
			stringToMap(resultData, Constants.POST_CNT);
		}
		
		formatItemUrl(resultData);
		getNextpageUrl(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	public void stringToMap(Map<String, Object> resultData, String key) {
		// 用户数 帖子数
		if (key.equals(Constants.POST_CNT)) {
			String sPostCnt = (String) resultData.get(key);
			String sUserCnt = (String) resultData.get(Constants.USER_CNT);
			sPostCnt = sPostCnt.replace(sUserCnt, "").trim();
			if(sUserCnt.contains("用户数：") && sUserCnt.contains("|")){
				sUserCnt = sUserCnt.replace("用户数：", "").replace("|", "").trim();	
			}
			if(sPostCnt.contains("共") && sPostCnt.contains("个帖子")){
				sPostCnt = sPostCnt.replace("共", "").replace("个帖子", "").trim();
			}
			resultData.put(Constants.POST_CNT, sPostCnt);
			resultData.put(Constants.USER_CNT, sUserCnt);
		}
	}
	
	/**
	 * 处理京东论坛的url格式问题
	 * http://group.jd.com/thread/20000151/20873465/20001111.htm --> http://group.jd.com/thread/20000151/20873465/c20001111/p1.htm
	 * @param result
	 */
	@SuppressWarnings("unchecked")
	private void formatItemUrl(Map<String, Object> resultData) {
		List<Map<String,Object>> itemsList = (List<Map<String,Object>>) resultData.get(Constants.ITEMS);
		List<Map<String,String>> tasksList = (ArrayList<Map<String,String>>) resultData.get(Constants.TASKS);
		for (int i = 0; itemsList != null && i < itemsList.size(); i++) {
			Map<String, Object> itemMap = itemsList.get(i);
			Map<String, String> linkMap = (Map<String, String>) itemMap
					.get(Constants.ITEMLINK);
			String sLink = linkMap.get(Constants.LINK);
			if (sLink == null) {
				continue;
			}
			matcher = pattern.matcher(sLink);
			if (matcher.find()) {
				sLink = matcher.group(1) + "c" + matcher.group(2)
						+ "/p1.htm";
				linkMap.put(Constants.LINK, sLink);
				linkMap.put(Constants.RAWLINK, sLink);
			}

		}
		
		for (int i = 0; tasksList != null && i < tasksList.size(); i++) {
			Map<String, String> tasksMap = tasksList.get(i);
			String sLink = tasksMap.get(Constants.LINK);
			if (sLink == null) {
				continue;
			}
			matcher = pattern.matcher(sLink);
			if (matcher.find()) {
				sLink = matcher.group(1) + "c" + matcher.group(2)
						+ "/p1.htm";
				tasksMap.put(Constants.LINK, sLink);
				tasksMap.put(Constants.RAWLINK, sLink);
			}
		}
		
	}
	
	/**
	 * 拼接下一页的URL的生成任务
	 * @param unit
	 * @param resultData
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void getNextpageUrl(ParseUnit unit, ParseResult result) {
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = (String) unit.getUrl();
		int postCnt = Integer.parseInt((String) resultData.get(Constants.POST_CNT)) ;
		
		Pattern pattern = Pattern.compile("(http://group.jd.com/[a-z]+/\\d+/\\d+)(?:/p)?(\\d+)?(?:/t-\\d+)?.htm");
		Matcher matcher = pattern.matcher(url);

		StringBuffer sb = new StringBuffer();
		int nextPage = 0;
		if (matcher.find()) {
			sb.append(matcher.group(1));
			String page = matcher.group(2);
			if(page != null){
				nextPage = Integer.parseInt(page)+1;
			}else{
				nextPage = 2;
			}
			sb.append("/p"+nextPage);
			sb.append("/t-1.htm");		
		}

		if (nextPage == 0) {
			return;
		}		
		int maxPage = 0;
		if (postCnt % 15 == 0) {
			maxPage = postCnt / 15;
		} else {
			maxPage = postCnt / 15 + 1;
		}
		
		Map nextpageTask = new HashMap();
		if(nextPage <= maxPage){
			String sNextpageUrl = sb.toString();
			nextpageTask.put(Constants.LINK, sNextpageUrl);
			nextpageTask.put(Constants.RAWLINK, sNextpageUrl);
			nextpageTask.put(Constants.LINKTYPE, "bbspostlist");
			if (resultData != null && !resultData.isEmpty()) {
				resultData.put(Constants.NEXTPAGE, sNextpageUrl);
				List<Map> tasks = (List<Map>) resultData.get(Constants.TASKS);
				tasks.add(nextpageTask);	
				ParseUtils.getIid(unit, result);
			}
		}
		
	}
}
