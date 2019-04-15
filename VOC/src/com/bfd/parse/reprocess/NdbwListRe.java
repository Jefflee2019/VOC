package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

public class NdbwListRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl();
		String pageData = unit.getPageData();
		/**
		 * function:拼接下一页链接 
		 * note:因为post请求的列表页，在线上环境不能下载，所以采用站内百度搜索
		 * http://zhannei.baidu.com/cse/search?q=%E5%8D%8E%E4%B8%BA&p=5&s=12984309719978984209&entry=1
		 */
		if (resultData != null && resultData.containsKey("xiayiye")) {
			String reg = "&p=(\\d+)&";
			Matcher mch = Pattern.compile(reg).matcher(url);
			if (mch.find()) {
				int pageNoCurrent = Integer.parseInt(mch.group(1));
				//控制页码最多翻74页，以防"下一页"出现问题而造成循环
				if(pageData.contains("下一页")&& pageNoCurrent <= 74) {
					String nextpage = url.replace("&p="+pageNoCurrent, "&p="+(pageNoCurrent+1));
					Map<String, Object> task = new HashMap<String, Object>();
					task.put("link", nextpage);
					task.put("rawlink", nextpage);
					task.put("linktype", "newslist");
					resultData.put("nextpage", nextpage);
					List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
					tasks.add(task);
					}
				}
		}
		/**
		 * function:拼接下一页链接 
		 * note1：以当页的新闻条目数判读是否生成下一页任务
		 * note2：搜索结果过多，控制翻页在75内(1年内)
		 *//*
		if (resultData != null && resultData.containsKey("items")) {
			// 页面新闻数
			int sizeNo = 20;
			List<Map<String, Object>> items = (List<Map<String, Object>>) resultData.get("items");
			if (items.size() == sizeNo) {
				String reg = "pageNoCurrent=(\\d+)&";
				Matcher mch = Pattern.compile(reg).matcher(url);
				if (mch.find()) {
					int pageNoCurrent = Integer.parseInt(mch.group(1));
					// 控制翻页深度
					if (pageNoCurrent < 75) {
						String nextpage = url.replace("pageNoCurrent=" + pageNoCurrent, "pageNoCurrent="
								+ (pageNoCurrent + 1));
						Map<String, Object> task = new HashMap<String, Object>();
						task.put("link", nextpage);
						task.put("rawlink", nextpage);
						task.put("linktype", "newslist");
						resultData.put("nextpage", nextpage);
						List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
						tasks.add(task);
					}
				}
			}
		}*/
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
