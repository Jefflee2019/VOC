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
 * 站点名：京东(论坛)
 * 
 * 主要功能：
 * 		处理浏览数，回复数
 *		并生成下一页链接。
 *		删除回复中重复的楼主信息
 *		
 * 
 * @author bfd_03
 *
 */
public class BjdPostRe_bak0321 implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		if (resultData != null && !resultData.isEmpty()) {
			//回复
			if (resultData.containsKey(Constants.REPLYS)) {
				stringToMap(resultData, unit, Constants.REPLYS);
			}
			//浏览数
			if (resultData.containsKey(Constants.VIEW_CNT)) {
				stringToMap(resultData, unit, Constants.VIEW_CNT);
			}
			//回复数
			if (resultData.containsKey(Constants.REPLYCOUNT)) {
				stringToMap(resultData, unit, Constants.REPLYCOUNT);
			}
			
		}
		getNextpageUrl(unit, result);
		return new ReProcessResult(processcode, processdata);
	}

	@SuppressWarnings("unchecked")
	public static void stringToMap(Map<String, Object> resultData, ParseUnit unit, String key) {
		if (key.equals(Constants.REPLYS)) {
			List<Map<String, String>> replyList = (List<Map<String, String>>) resultData.get(key);
			Map<String, String> replyMap = null;		
			
			for (int i = 0; i < replyList.size();) {
				replyMap = replyList.get(i);
				if (replyList.get(i) instanceof Map) {
					replyMap = replyList.get(i);
					String replyfloor = replyMap.get(Constants.REPLYFLOOR);

					if (replyfloor.equals("楼主")) {
						replyList.remove(i);
						continue;
					}

					if (replyfloor.indexOf("楼") > 0) {
						replyfloor = replyfloor.replace("楼", "");
					}
					
					replyMap.put(Constants.REPLYFLOOR, replyfloor);
				}
				i++;
			}
			
			resultData.put(Constants.REPLYS, replyList);
		}

		if(key.equals(Constants.VIEW_CNT)){
			String sViewCnt = (String)resultData.get(key);
			if(sViewCnt.contains("浏览：")){
				sViewCnt = sViewCnt.replace("浏览：", "").trim();
			}
			resultData.put(Constants.VIEW_CNT, sViewCnt);
		}
		
		if(key.equals(Constants.REPLYCOUNT)){
			String replycount = (String)resultData.get(key);
			if(replycount.contains("回复：")){
				replycount = replycount.replace("回复：", "").trim();
			}
			resultData.put(Constants.REPLYCOUNT, replycount);
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
		int replycount = Integer.parseInt((String) resultData.get(Constants.REPLYCOUNT)) ;
		
		Pattern pattern = Pattern.compile("(http://group.jd.com/[a-z]+/\\d+/\\d+/)(\\w+)(?:/p)?(\\d+)?.htm");
		Matcher matcher = pattern.matcher(url);

		StringBuffer sb = new StringBuffer();
		int nextPage = 0;
		if (matcher.find()) {
			sb.append(matcher.group(1));
			String page = matcher.group(3);
			if(page != null){
				nextPage = Integer.parseInt(page)+1;
				sb.append(matcher.group(2));
			}else{
				nextPage = 2;
				sb.append("c"+matcher.group(2));
			}
			sb.append("/p"+nextPage);
			sb.append(".htm");		
		}

		if (nextPage == 0) {
			return;
		}		
		int maxPage = 0;
		if (replycount % 10 == 0) {
			maxPage = replycount / 10;
		} else {
			maxPage = replycount / 10 + 1;
		}
		
		Map nextpageTask = new HashMap();
		if(nextPage <= maxPage){
			String sNextpageUrl = sb.toString();
			nextpageTask.put(Constants.LINK, sNextpageUrl);
			nextpageTask.put(Constants.RAWLINK, sNextpageUrl);
			nextpageTask.put(Constants.LINKTYPE, "bbspost");
			if (resultData != null && !resultData.isEmpty()) {
				resultData.put(Constants.NEXTPAGE, sNextpageUrl);
				List<Map> tasks = (List<Map>) resultData.get(Constants.TASKS);
				tasks.add(nextpageTask);	
				ParseUtils.getIid(unit, result);
			}
		}
		
	}

}
