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
 * 站点名：京东
 * 
 * 主要功能：生成列表页的下一页的链接
 * 
 * @author bfd_03
 *
 */
public class EjingpinListRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		// litaihua
		List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
		for (Map<String, Object> map : tasks) {
			String link = (String) map.get("link");
			link = link + "?d=d";
			map.put("link", link);
			map.put("rawlink", link);
		}

		getNextpageUrl(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	/**
	 * 拼接下一页的URL的生成任务
	 * 
	 * @param unit
	 * @param resultData
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void getNextpageUrl(ParseUnit unit, ParseResult result) {
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = (String) unit.getUrl();
		String pageData = unit.getPageData();

		// http://search.jd.com/s_new.php?keyword=%E5%8D%8E%E4%B8%BA&enc=utf-8&stop=1&vt=2&page=1
		// 只有搜索列表页需要后处理插件生成下一页链接 ————>所有列表页都需要生成下一页链接
		if (url.contains("search.jd.com")) {
			Pattern pattern = Pattern.compile("&page=(\\d+)");
			Matcher matcher = pattern.matcher(url);

			StringBuffer sb = new StringBuffer();
			int curPage = 0;
			if (matcher.find()) {
				curPage = Integer.parseInt(matcher.group(1));
			}

			if (curPage == 0 || curPage >= 200) {
				return;
			}

			int index = url.lastIndexOf("=");
			url = url.substring(0, index + 1);
			sb.append(url);
			sb.append(curPage + 1);

			// 判断当前页面中商品数量，小于30则当前页为最后一页
			pattern = Pattern.compile("class=\"gl-item\"", Pattern.DOTALL);
			matcher = pattern.matcher(pageData);
			int count = 0;
			while (matcher.find()) {
				count++;
			}

			Map nextpageTask = new HashMap();
			if (count >= 30) {
				String sNextpageUrl = sb.toString();
				nextpageTask.put(Constants.LINK, sNextpageUrl);
				nextpageTask.put(Constants.RAWLINK, sNextpageUrl);
				nextpageTask.put(Constants.LINKTYPE, "eclist");
				if (resultData != null && !resultData.isEmpty()) {
					resultData.put(Constants.NEXTPAGE, sNextpageUrl);
					List<Map> tasks = (List<Map>) resultData.get(Constants.TASKS);
					tasks.add(nextpageTask);
					ParseUtils.getIid(unit, result);
				}
			}
		} else if (url.contains("list.jd.com")) {
			Pattern pattern = Pattern.compile("&page=(\\d+)");
			Matcher matcher = pattern.matcher(url);
			int curPage = 0;
			if (matcher.find()) {
				curPage = Integer.parseInt(matcher.group(1));
			}
			//控制翻页，以免下一页控制失效，而造成无限翻页。
			//因为列表页在随机页码会发生混乱，从指定品牌跳转到产品主页。如"笔记本>华为"列表页第5页，可能会跳到"笔记本"列表页第一页
			if (pageData.contains(">下一页<")&&curPage<49) {
				Map nextpageTask = new HashMap();
				String sNextpageUrl = url.replace("&page=" + curPage, "&page=" + (curPage + 1));
				nextpageTask.put(Constants.LINK, sNextpageUrl);
				nextpageTask.put(Constants.RAWLINK, sNextpageUrl);
				nextpageTask.put(Constants.LINKTYPE, "eclist");
				if (resultData != null && !resultData.isEmpty()) {
					resultData.put(Constants.NEXTPAGE, sNextpageUrl);
					List<Map> tasks = (List<Map>) resultData.get(Constants.TASKS);
					tasks.add(nextpageTask);
//					ParseUtils.getIid(unit, result);
				}
			}
		}
	}
}
