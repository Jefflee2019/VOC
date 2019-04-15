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
 * @site：站长网(Nadmin5)
 * @function 列表页后处理插件，deal with 下一页循环翻页问题及无关的url过滤
 * @author bfd_02
 *
 */
public class Nadmin5ListRe implements ReProcessor {

	@SuppressWarnings("rawtypes")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		String pageData = unit.getPageData();
		if (pageData.contains(">下一页")) {
			String url = (String) unit.getTaskdata().get("url");
			Pattern ptn = Pattern.compile("&p=(\\d+)&");
			Matcher m = ptn.matcher(url);
			if (m.find()) {
				int p = Integer.parseInt(m.group(1));
				// p=74 对应的是第75页，下一页会循环到第1页
				if (p >= 74) {
					resultData.remove(Constants.NEXTPAGE);
					List tasks = (ArrayList) resultData.get(Constants.TASKS);
					for (int i = 0; i < tasks.size(); i++) {
						if (((Map) tasks.get(i)).get(Constants.LINKTYPE).equals("newslist")) {
							tasks.remove(i);
						}
					}
				}
			}
		}

		// 过滤打开是列表页的url
		// 处理items
		if (resultData.containsKey(Constants.ITEMS)) {
			List items = (List) resultData.get(Constants.ITEMS);
			for (int i = 0; i < items.size(); i++) {
				Map itemMap = (Map) items.get(i);
				if (itemMap.containsKey(Constants.LINK)) {
					Map linkMap = (Map) itemMap.get(Constants.LINK);
					String link = linkMap.get(Constants.LINK).toString();
					if (!link.contains("/article/")) {
						items.remove(itemMap);
						i--;
					}
				}
			}
		}

		// 处理tasks
		if (resultData.containsKey(Constants.TASKS)) {
			List tasks = (List) resultData.get(Constants.TASKS);
			for (int i = 0; i < tasks.size(); i++) {
				Map taskMap = (Map) tasks.get(i);
				String linktype = taskMap.get(Constants.LINKTYPE).toString();
				String link = taskMap.get(Constants.LINK).toString();
				if (linktype.equals("newscontent") && !link.contains("/article/")) {
					tasks.remove(taskMap);
					i--;
				}
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}

}
