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
 * 说明：Ejd2类是针对京东在手机版块下搜索关键字的前10页结果
 * 列表页录入的时候需要加上page=1
 * 列表页插件中生成判断商品总页数并生成下一页链接
 * @author bfd_01
 *
 */
public class Ejdxiaomi2ListRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
//		Map<String, Object> resultData = result.getParsedata().getData();
		// litaihua
//		List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
//		for (Map<String, Object> map : tasks) {
//			String link = (String) map.get("link");
//			link = link + "?e=e";
//			map.put("link", link);
//			map.put("rawlink", link);
//		}

		getNextpageUrl(unit, result);
		ParseUtils.getIid(unit, result);
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

		// https://list.jd.com/list.html?cat=9987,653,655&ev=exbrand%5F18374&page=1&stock=0&sort=sort_rank_asc&trans=1&JL=6_0_0&ms=6#J_main
		// 
		if (url.contains("list.jd.com")) {
			Pattern pattern = Pattern.compile("&page=(\\d+)");
			Matcher matcher = pattern.matcher(url);
			int curPage = 1;
			if (matcher.find()) {
				curPage = Integer.parseInt(matcher.group(1));
			}
			// 控制翻页，以免下一页控制失效，而造成无限翻页。
			// 商品总数
			int page = 1;
			if (resultData.containsKey("total_cnt")) {
				String cnt = resultData.get("total_cnt").toString();
				page = Integer.valueOf(cnt);
			}
			if (page > curPage) {
				Map nextpageTask = new HashMap();
				String sNextpageUrl = url.replace("&page=" + curPage, "&page=" + (curPage+1));
				nextpageTask.put(Constants.LINK, sNextpageUrl);
				nextpageTask.put(Constants.RAWLINK, sNextpageUrl);
				nextpageTask.put(Constants.LINKTYPE, "eclist");
				if (resultData != null && !resultData.isEmpty()) {
					resultData.put(Constants.NEXTPAGE, sNextpageUrl);
					List<Map> tasks = (List<Map>) resultData.get(Constants.TASKS);
					tasks.add(nextpageTask);
				}
			}
		}
	}
}
