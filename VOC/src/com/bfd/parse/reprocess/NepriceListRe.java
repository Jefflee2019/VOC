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
 * @site：Neprice
 * @function 列表页后处理插件，处理下一页
 * @author bfd_02
 *
 */
public class NepriceListRe implements ReProcessor {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
//		String pageData = unit.getPageData();
		String nextpage = null;
		if (resultData != null && !resultData.isEmpty()) {
			String nextPage = (String) resultData.get("next_page");
			if (nextPage != null && nextPage.contains("下一")) {
				// 处理下一页链接
				Map<String, Object> nextpageTask = new HashMap<String, Object>();
				String url = unit.getUrl();
				int oldPageNum = getPageNum(url); //列表页页数是必有项，这里不会返回0
				nextpage = url.replace("/all/" + oldPageNum, "/all/" + (oldPageNum + 1));

				nextpageTask.put("link", nextpage);
				nextpageTask.put("rawlink", nextpage);
				nextpageTask.put("linktype", "newslist");
				resultData.put("nextpage", nextpage);
				List<Map> tasks = (List<Map>) resultData.get("tasks");
				tasks.add(nextpageTask);

				//注释时间：2017-02-25 删除原来360搜索时的处理
				/*if (resultData != null && !resultData.isEmpty()) {
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
				}*/
			}

			ParseUtils.getIid(unit, result);
		}

		return new ReProcessResult(SUCCESS, processdata);
	}

	private int getPageNum(String url) {
		Pattern iidPatter = Pattern.compile("/all/(\\d+)");
		Matcher match = iidPatter.matcher(url);
		if (match.find()) {
			return Integer.parseInt(match.group(1));
		} else {
			return 0;
		}
	}

//	private void decodeLink(Map<String, Object> task, String link) {
//		try {
//			if (link.contains("url=")) {
//				Matcher match = url_Pattern.matcher(link);
//				if (match.find()) {
//					link = match.group(0);
//					link = link.replaceAll("url=", "");
//					link = link.replaceAll("&q=", "");
//				}
//				link = URLDecoder.decode(link, "utf-8");
//				task.put(Constants.LINK, link);
//				task.put(Constants.RAWLINK, link);
//			}
//		} catch (UnsupportedEncodingException e) {
//		}
//	}
}
