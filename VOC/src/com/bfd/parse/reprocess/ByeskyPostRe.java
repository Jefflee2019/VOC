package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
/**
 * 站点名：天极网-论坛
 * 主要功能：处理楼层数，主贴与回复，帖子内容等
 * @author bfd_01
 *
 */
public class ByeskyPostRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();

		if (!resultData.isEmpty()) {
			
			// 处理楼层数
			if (resultData.containsKey(Constants.REPLYS)) {
				List<Object> replys = (List<Object>) resultData.get("replys");
				// 处理主贴信息
				if (((Map<String,Object>)replys.get(0)).get("replyfloor").equals("主贴")) {
					replys.remove(0);
					dealTopic(resultData);
					
				} else {
					// 删除主贴相关内容
					resultData.remove(Constants.NEWSTIME);
					resultData.remove(Constants.CONTENTS);
					resultData.remove(Constants.AUTHOR);
				}
				dealReplys(resultData);
				for (Object obj : replys) {
					if (obj instanceof Map) {
						Map<String, Object> reply = (Map<String, Object>) obj;
						if (reply.containsKey("replyfloor")) {
							String replyfloor = reply.get("replyfloor")
									.toString();
							replyfloor = replyfloor.replace("楼主", "1")
									.replace("沙发", "2").replace("板凳", "3")
									.replace("楼", "");
							reply.put("replyfloor", replyfloor);
						}
					}
				}
			}
		}
		return new ReProcessResult(processcode, processdata);
	}
	
	/**
	 * 处理主贴内容
	 * @param resultData
	 */
	@SuppressWarnings("unchecked")
	private void dealTopic(Map<String,Object> resultData) {
		if (resultData.containsKey(Constants.AUTHOR)) {
			List<Map<String,Object>> authorList = (List<Map<String,Object>>) resultData.get(Constants.AUTHOR);
			Map<String,Object> author = (Map<String,Object>) authorList.get(0);
			if (author.containsKey(Constants.CHECKIN_DAYS)) {
			String checkinDays = author.get(Constants.CHECKIN_DAYS).toString();
			if ("该用户从未签到".equals(checkinDays)) {
				author.put(Constants.CHECKIN_DAYS, 0);
			} else {
				Pattern p = Pattern.compile("签到天数: (\\d+) 天");
				Matcher m = p.matcher(checkinDays);
				while (m.find()) {
					author.put(Constants.CHECKIN_DAYS,
							Integer.valueOf(m.group(1)));
				}
			}
			}
		}
		if (resultData.containsKey(Constants.NEWSTIME)) {
			String newstime = resultData.get(Constants.NEWSTIME).toString();
			newstime = newstime.replace("发表于 ", "");
			resultData.put(Constants.NEWSTIME, ConstantFunc.convertTime(newstime));
		}
		if (resultData.containsKey(Constants.CONTENTS)) {
			String contents = resultData.get(Constants.CONTENTS).toString();
			contents = contents.replaceAll("电梯直达.* 阅读模式", "");
			resultData.put(Constants.CONTENTS, contents.substring(1));
		}
		
	}
	
	/**
	 * 处理作者的签到字段
	 * @param resultData
	 */
	@SuppressWarnings("unchecked")
	private void dealReplys(Map<String,Object> resultData) {
		if (resultData.containsKey(Constants.REPLYS)) {
			List<Map<String,Object>> replysList = (List<Map<String,Object>>) resultData.get(Constants.REPLYS);
			for (int i = 0; i < replysList.size(); i++) {
				Map<String,Object> replys = (Map<String,Object>) replysList.get(i);
//				String checkinDays = replys.get(Constants.REPLY_CHECKIN_DAYS)
//						.toString();
//				if ("该用户从未签到".equals(checkinDays)) {
//					replys.put(Constants.REPLY_CHECKIN_DAYS, 0);
//				} else {
//					Pattern p = Pattern.compile("签到天数: (\\d+) 天");
//					Matcher m = p.matcher(checkinDays);
//					while (m.find()) {
//						replys.put(Constants.REPLY_CHECKIN_DAYS,
//								Integer.valueOf(m.group(1)));
//					}
//				}
				String replydate = replys.get(Constants.REPLYDATE).toString();
				replydate = replydate.replace("发表于 ", "");
				replys.put(Constants.REPLYDATE,
						ConstantFunc.convertTime(replydate));
				if (replys.get(Constants.REPLYCONTENT).toString()
						.startsWith(": ")) {
					replys.put(Constants.REPLYCONTENT,
							replys.get(Constants.REPLYCONTENT).toString()
									.substring(2));
				}
			}
		}
	}
}
