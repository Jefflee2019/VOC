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
 * @site：Ncctv
 * @function 提取内容页链接
 * @author bfd_02
 *
 */
public class NcctvListRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		// 源码中的新闻链接有误，需要截取
		List<Map<String, Object>> itemsList = null;
		List<Map<String, Object>> taskList = null;
		if (resultData.containsKey(Constants.ITEMS) && resultData.get(Constants.ITEMS) instanceof List) {
			itemsList = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
			if (itemsList != null && !itemsList.isEmpty()) {
				for (Map<String, Object> itemMap : itemsList) {
					if (itemMap.containsKey("link")) {
						Map<String, Object> linkMap = (Map<String, Object>) itemMap.get("link");
						if (linkMap.containsKey("link")) {
							String link = linkMap.get("link").toString();
							link = linkFromRex("targetpage=(\\S*)&point", link);
							linkMap.put("link", link);
							linkMap.put("rawlink", link);
						}
					}
				}

			}
		}
		
		if (resultData.containsKey(Constants.TASKS) && resultData.get(Constants.TASKS) instanceof List) {
			taskList = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
			if (taskList != null && !taskList.isEmpty()) {
				for (Map<String, Object> taskMap : taskList) {
					if (taskMap.containsKey("linktype") && !("newslist").equals(taskMap.get("linktype"))) {
						String link = taskMap.get("link").toString();
						link = linkFromRex("targetpage=(\\S*)&point", link);
						taskMap.put("link", link);
						taskMap.put("rawlink", link);
					}
				}

			}
		}

		 ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}

	private String linkFromRex(String rex, String link) {
		String url = null;
		Matcher match = Pattern.compile(rex).matcher(link);
		if (match.find()) {
			url = match.group(1);
		}
		return url;
	}

}
