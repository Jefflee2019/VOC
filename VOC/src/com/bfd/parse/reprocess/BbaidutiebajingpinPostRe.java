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

public class BbaidutiebajingpinPostRe implements ReProcessor {

	/**
	 * situation: 百度贴吧存在两种模板，其中一类根目录比另外一类多一层div结构 
	 * note:【重要】如果涉及到模板id的修改，应该相应修改代码中关于模板id的判断(一定要对应相应环境上的模板id)
	 */

	@SuppressWarnings("unchecked")
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String pageData = unit.getPageData();
		
		// 通过模板id区别模板
		int tmpl_id = (int) resultData.get("tmpl_id");
		// 可以标出发表时间、楼层的模板
//		if (tmpl_id == 2530) {
		if (tmpl_id == 2170) {
			// 这类模板的楼层、发表时间都不可见，需要正则匹配出来
			// 1、匹配出所有楼层
			// &quot;post_no&quot;:157,&quot;
			String floorRegex = "&quot;post_no&quot;:(\\d+)";
			List<String> floorList = floorAndTimeReg(pageData, floorRegex);
			// 2、匹配出所有的发表时间
			// &quot;date&quot;:&quot;2016-08-06 16:56&quot
			String timeRegex = "&quot;date&quot;:&quot;([\\d\\s-\\:]+)&quot";
			List<String> timeList = floorAndTimeReg(pageData, timeRegex);
			if (resultData.containsKey(Constants.REPLYS)) {
				List<Object> replysList = (List<Object>) resultData.get(Constants.REPLYS);
				for (int i = 0; i < replysList.size(); i++) {
					Object obj = replysList.get(i);
					// 过滤掉广告
					if (obj instanceof String) {
						replysList.remove(obj);
						i--;
					} else if (obj instanceof Map) {
						Map<String, Object> map = (Map<String, Object>) replysList.get(i);

						if (floorList.get(i).equals("1")) {
							resultData.put(Constants.NEWSTIME, timeList.get(i));
							replysList.remove(map);
							floorList.remove(floorList.get(i));
							timeList.remove(timeList.get(i));
							i--;
						} else {
							map.put(Constants.REPLYFLOOR, floorList.get(i));
							map.put(Constants.REPLYDATE, timeList.get(i));
						}
					}
				}
			}
//		} else if(tmpl_id == 2532) {
		} else if(tmpl_id == 2171) {
			if (resultData.containsKey(Constants.REPLYS)) {
				List<Object> replysList = (List<Object>) resultData.get(Constants.REPLYS);
				for (int i = 0; i < replysList.size(); i++) {
					Object obj = replysList.get(i);
					// 过滤掉广告
					if (obj instanceof String) {
						replysList.remove(obj);
						i--;
					} else if (obj instanceof Map) {
						Map<String, Object> map = (Map<String, Object>) replysList.get(i);
						String replyFloor="";
						if (!map.containsKey(Constants.REPLYDATE) && map.containsKey(Constants.REPLYFLOOR)
								&& map.containsKey(Constants.SOURCE)) {
							// 如果没有发表时间字段，则说明页面该楼层没有来源，将当前来源字段的值赋给楼层，楼层的值赋给发表时间
							replyFloor = map.get(Constants.REPLYFLOOR).toString();
							String source = map.get(Constants.SOURCE).toString();
							String replyDate = replyFloor;
							map.put(Constants.REPLYDATE, replyDate);
							replyFloor = source.replace("楼", "").trim();
							map.put(Constants.REPLYFLOOR, replyFloor);
							map.remove(Constants.SOURCE);
						} else if (map.containsKey(Constants.REPLYDATE) && map.containsKey(Constants.REPLYFLOOR)
								&& map.containsKey(Constants.SOURCE)) {
							replyFloor = map.get(Constants.REPLYFLOOR).toString();
							replyFloor = replyFloor.replace("楼", "").trim();
							map.put(Constants.REPLYFLOOR, replyFloor);
							map.remove(Constants.SOURCE);
						}
						if(replyFloor.equals("1")){
							resultData.put(Constants.NEWSTIME, map.get(Constants.REPLYDATE));
							replysList.remove(map);
							i--;
						}
					}
				}
			}
		}

		// 处理下一页
		String url = unit.getUrl();
		if (pageData.contains("下一页")) {
			String nextPage = "";
			if (url.contains("pn=")) {
				String nextPageReg = "pn=(\\d+)";
				Matcher match = Pattern.compile(nextPageReg).matcher(url);
				if (match.find()) {
					int pageid = Integer.parseInt(match.group(1));
					nextPage = url.replace("pn=" + pageid, "pn=" + (pageid + 1));
				}
			} else {
				nextPage = url + "?pn=2";
			}
			Map<String, Object> taskMap = new HashMap<String, Object>();
			taskMap.put(Constants.LINK, nextPage);
			taskMap.put(Constants.RAWLINK, nextPage);
			taskMap.put(Constants.LINKTYPE, "bbspost");
			resultData.put(Constants.NEXTPAGE, nextPage);

			List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
			if (tasks == null) {
				tasks = new ArrayList<Map<String, Object>>();
				resultData.put(Constants.TASKS, tasks);
			}
			tasks.add(taskMap);
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}

	private List<String> floorAndTimeReg(String pageData, String floorRegex) {
		Matcher match = Pattern.compile(floorRegex).matcher(pageData);
		List<String> floorOrTimeList = new ArrayList<String>();
		while (match.find()) {
			// 匹配出所有楼层
			floorOrTimeList.add(match.group(1));
		}
		return floorOrTimeList;
	}
}
