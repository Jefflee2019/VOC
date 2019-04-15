package com.bfd.parse.reprocess;

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
 * @site:新浪网-论坛
 * @function 论坛列表页后处理插件 添加回复数
 * 
 * @author bfd_02
 *
 */

public class BsinaListRe implements ReProcessor {
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(BsinaListRe.class);
//	http://club.tech.sina.com.cn/mobile/viewthread.php?tid=13543565&extra=page%3D1&page=9
//		http://club.tech.sina.com.cn/mobile/thread-13543565-1-1.html
	private static final Pattern TID_PATTERN = Pattern.compile("thread-(\\d+)");
	private static final Pattern PAGE_PATTERN = Pattern.compile("-(\\d+)-\\d+\\.html");
	private static Matcher tidMatcher = null;
	private static Matcher pageMatcher = null;
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();
		
		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			return null;
		}
		// add reply_cnt
		if (resultData.containsKey(Constants.TASKS)) {
			List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData
					.get(Constants.TASKS);
			if (tasks != null && !tasks.isEmpty()) {
				for (Map<String, Object> item : tasks) {
					if(item.containsKey("link")) {
						String link = item.get("link").toString();
						tidMatcher = TID_PATTERN.matcher(link);
						pageMatcher = PAGE_PATTERN.matcher(link);
						if(tidMatcher.find() && pageMatcher.find()) {
							String tid = tidMatcher.group(1);
							String page = pageMatcher.group(1);
							StringBuilder linkSb = new StringBuilder();
							linkSb.append("http://club.tech.sina.com.cn/mobile/viewthread.php?tid=");
							linkSb.append(tid);
							linkSb.append("&extra=page%3D1&page=");
							linkSb.append(page);
							item.put("link", linkSb.toString());
						}
					}
				}
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
