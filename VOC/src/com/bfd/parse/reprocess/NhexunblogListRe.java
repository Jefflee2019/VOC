package com.bfd.parse.reprocess;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
 * @site：Nifeng
 * @function 列表页后处理插件，deal with 75页的nextpage会循环第一页
 * @author bfd_03
 *
 */
public class NhexunblogListRe implements ReProcessor {

	private static final Pattern url_Pattern = Pattern.compile("url=.*&q=");

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = (String) unit.getTaskdata().get("url");
		String nextpage = null;
		if (resultData != null && !resultData.isEmpty()) {
			if (url.contains("www.so.com")) {
				// 处理下一页链接
				Map<String, Object> nextpageTask = new HashMap<String, Object>();
				String oldPageNum = getPage(url);
				if (oldPageNum.equals("0")) {
					nextpage = url + "&pn=2";
				} else {
					int pageNum = Integer.valueOf(oldPageNum) + 1;
					nextpage = url.replace("&pn=" + oldPageNum, "&pn=" + pageNum);
				}

				nextpageTask.put("link", nextpage);
				nextpageTask.put("rawlink", nextpage);
				nextpageTask.put("linktype", "newslist");
				resultData.put("nextpage", nextpage);
				List<Map> tasks = (List<Map>) resultData.get("tasks");
				tasks.add(nextpageTask);

				if (resultData != null && !resultData.isEmpty()) {
					// 做url的截取，避免url中每次都变得部分导致列表页一直刷
					if (resultData.containsKey(Constants.TASKS)) {
						List<Map<String, Object>> tasks2 = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
						for (Map<String, Object> task : tasks2) {
							if (task.containsKey(Constants.LINK)) {
								String link = task.get(Constants.LINK).toString();
								decodeLink(task, link);
							}
						}
					}
				}
			}
			ParseUtils.getIid(unit, result);
		}

		return new ReProcessResult(processcode, processdata);
	}

	private String getPage(String url) {
		if (url.contains("www.so.com")) {
			Pattern iidPatter = Pattern.compile("&pn=(\\d+)");
			Matcher match = iidPatter.matcher(url);
			if (match.find()) {
				return match.group(1);
			} else {
				return "0";
			}
		}
		return null;
	}

	private void decodeLink(Map<String, Object> task, String link) {
		// 截取url
		try {
			if (link.contains("url=")) {
				Matcher match = url_Pattern.matcher(link);
				if (match.find()) {
					link = match.group(0);
					link = link.replaceAll("url=", "");
					link = link.replaceAll("&q=", "");
				}
				link = URLDecoder.decode(link, "utf-8");
				task.put(Constants.LINK, link);
				task.put(Constants.RAWLINK, link);
			}
		} catch (UnsupportedEncodingException e) {
		}
	}
}
