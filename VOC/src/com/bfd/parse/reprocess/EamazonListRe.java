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

/**
 * 站点名：Eamazon
 * 
 * 功能：列表页去重
 * 
 * @author bfd_06
 */
public class EamazonListRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		// 去掉items内重复链接
		List<Map<String, Object>> items = (List<Map<String, Object>>) resultData
				.get("items");
		Map<String, Map<String, Object>> iMap = new HashMap<String, Map<String, Object>>();
		for (Map<String, Object> item : items) {
			Map<String, Object> itemlink = (Map<String, Object>) item
					.get("itemlink");
			iMap.put((String) itemlink.get("link"), item);
		}
		items.clear();
		items.addAll(iMap.values());
		for (Map<String, Object> item : items) {
			Map<String, Object> itemlink = (Map<String, Object>) item
					.get("itemlink");
			String link = (String) itemlink.get("link");
//			String qid = match("&qid=(\\d+)", link);
//			if (qid != null) {
//				link = link.replace("&qid=" + qid, "");
//				itemlink.put("link", link);
//				itemlink.put("rawlink", link);
//			}
			link = link.split("[?]")[0];
			itemlink.put("link", link);
			itemlink.put("rawlink", link);
		}
		// 重新添加tasks
		List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData
				.get("tasks");
		tasks.clear();
		for (Map<String, Object> item : items) {
			Map<String, Object> itemlink = (Map<String, Object>) item
					.get("itemlink");
			Map<String, Object> task = new HashMap<String, Object>();
			task.put("link", itemlink.get("link"));
			task.put("rawlink", itemlink.get("rawlink"));
			task.put("linktype", itemlink.get("linktype"));
			tasks.add(task);
		}

		if (resultData.containsKey("nextpage")) {
			Object nextpage = resultData.get("nextpage");
			if (nextpage instanceof Map) {
				Map<String, Object> nextpageMap = (Map<String, Object>) nextpage;
				String link = (String) nextpageMap.get("link");
				String qid = match("&qid=(\\d+)", link);
				if (qid != null) {
					link = link.replace("&qid=" + qid, "");
					nextpageMap.put("link", link);
					nextpageMap.put("rawlink", link);
				}
//				link = link.split("[?]")[0];
//				nextpageMap.put("link", link);
//				nextpageMap.put("rawlink", link);
				Map<String, Object> task = new HashMap<String, Object>();
				task.put("link", link);
				task.put("rawlink", link);
				task.put("linktype", "eclist");
				tasks.add(task);
			} else {
				String nextPageUrl = (String) nextpage;
				String qid = match("&qid=(\\d+)", nextPageUrl);
				if (qid != null) {
					nextPageUrl = nextPageUrl.replace("&qid=" + qid, "");
					resultData.put("nextpage", nextPageUrl);
				}
				Map<String, Object> task = new HashMap<String, Object>();
				task.put("link", nextPageUrl);
				task.put("rawlink", nextPageUrl);
				task.put("linktype", "eclist");
				tasks.add(task);
			}
		}
		ParseUtils.getIid(unit, result);

		return new ReProcessResult(processcode, processdata);
	}

	public String match(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return matcher.group(1);
		}

		return null;
	}

}
