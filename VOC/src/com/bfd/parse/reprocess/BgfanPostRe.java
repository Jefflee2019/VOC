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

/**
 * 站点名：机锋网(论坛)
 * 
 * 主要功能：处理发表时间，回复（回复时间，回复楼层，签到天数）
 * 		     删除回复中重复的楼主信息
 * 		     删除第一页以后出现的楼主信息，主贴信息
 * 
 * @author bfd_03
 *
 */
public class BgfanPostRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		if (resultData != null && !resultData.isEmpty()) {
			//回复（回复内容，回复时间）
			if (resultData.containsKey(Constants.REPLYS)) {
				stringToMap(resultData, unit, Constants.REPLYS);
			}
			//发表时间
			if (resultData.containsKey(Constants.NEWSTIME)) {
				stringToMap(resultData, unit, Constants.NEWSTIME);
			}
			//作者
			if (resultData.containsKey(Constants.AUTHOR)) {
				stringToMap(resultData, unit, Constants.AUTHOR);
			}
			

		}

		return new ReProcessResult(processcode, processdata);
	}

	@SuppressWarnings("unchecked")
	public static void stringToMap(Map<String, Object> resultData, ParseUnit unit, String key) {
		String url = unit.getUrl();
		int curPage = 0;
		Pattern pattern = Pattern.compile("http://bbs.gfan.com/\\w+-\\d+-(\\d+)-1.html");
		Matcher matcher = pattern.matcher(url);
		if(matcher.find()){
			curPage = Integer.parseInt(matcher.group(1));
		}else{
			curPage = 1; //默认当前页面第一页
		}
		
		if(key.equals(Constants.AUTHOR)){
			if(curPage > 1){
				resultData.remove(Constants.AUTHOR);
				resultData.remove(Constants.CONTENTS);
				resultData.remove(Constants.NEWSTIME);			
			}else{
				List<Map<String, String>> replyList = (List<Map<String, String>>) resultData.get(key);
				Map<String, String> authorMap = replyList.get(0);
				String sCheckinDays = authorMap.get(Constants.CHECKIN_DAYS);
				sCheckinDays = sCheckinDays.replace("签到天数:", "").replace("天", "").trim();
				authorMap.put(Constants.CHECKIN_DAYS, sCheckinDays);
			}
		}
		
		if (key.equals(Constants.REPLYS)) {
			List<Map<String, String>> replyList = (List<Map<String, String>>) resultData.get(key);
			Map<String, String> replyMap = null;		
			
			for (int i = 0; i < replyList.size();) {
				replyMap = replyList.get(i);
				if (replyList.get(i) instanceof Map) {
					replyMap = replyList.get(i);
					String replyfloor = replyMap.get(Constants.REPLYFLOOR);
					String replydate = replyMap.get(Constants.REPLYDATE);
					String sReplyCheckinDays = replyMap.get(Constants.REPLY_CHECKIN_DAYS);

					if (replyfloor.equals("楼主") || replyfloor.equals("楼")) {
						replyList.remove(i);
						continue;
					}

					int index = -1;
					if ((index = replydate.indexOf("发表于")) >= 0) {
						replydate = replydate.substring(index + 3).trim();
					}

					if (replyfloor.indexOf("楼") > 0) {
						replyfloor = replyfloor.replace("楼", "");
					}
					replyfloor = replyfloor.replace("楼主", "1")
							.replace("沙发", "2").replace("板凳", "3")
							.replace("地板", "4");

					sReplyCheckinDays = sReplyCheckinDays
							.replace("签到天数:", "").replace("天", "").trim();

					replyMap.put(Constants.REPLYDATE, replydate);
					replyMap.put(Constants.REPLYFLOOR, replyfloor);
					replyMap.put(Constants.REPLY_CHECKIN_DAYS, sReplyCheckinDays);
				}
				i++;
			}
			
			resultData.put(Constants.REPLYS, replyList);
		}

		if(key.equals(Constants.NEWSTIME)){
			String replydate = (String)resultData.get(key);
			int index = -1;
			if ((index = replydate.indexOf("发表于")) >= 0) {
				replydate = replydate.substring(index + 3)
						.trim();
			}
			resultData.put(Constants.NEWSTIME, replydate);
		}

	}

}
