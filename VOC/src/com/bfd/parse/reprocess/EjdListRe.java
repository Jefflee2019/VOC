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
 * 说明：Ejd1类是针对京东的店铺内的商品进行抓取
 * 列表页插件中生成判断商品总页数并生成下一页链接
 * @author bfd_01
 *
 */
public class EjdListRe implements ReProcessor {

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

		// https://search.jd.com/Search?keyword=%E5%8D%8E%E4%B8%BA&enc=utf-8&wq=%E5%8D%8E%E4%B8%BA&page=1
		if (url.contains("search.jd.com")) {
			Pattern pattern = Pattern.compile("page=(\\d+)");
			Matcher matcher = pattern.matcher(url);
			int curPage = 1;
			if (matcher.find()) {
				curPage = Integer.parseInt(matcher.group(1));
			}
			// 控制翻页，以免下一页控制失效，而造成无限翻页。
			// 商品总页数
			int total = 0;
			if (resultData.containsKey("pagecount")) {
				total = Integer.valueOf(resultData.get("pagecount").toString());
			}
			resultData.remove("pagecount");
			if (total*2 > curPage) {
				Map nextpageTask = new HashMap();
				String sNextpageUrl = url.replace("page=" + curPage,
						"page=" + (curPage + 2));
				nextpageTask.put(Constants.LINK, sNextpageUrl);
				nextpageTask.put(Constants.RAWLINK, sNextpageUrl);
				nextpageTask.put(Constants.LINKTYPE, "eclist");
				if (resultData != null && !resultData.isEmpty()) {
					resultData.put(Constants.NEXTPAGE, sNextpageUrl);
					List<Map> tasks = (List<Map>) resultData
							.get(Constants.TASKS);
					tasks.add(nextpageTask);
				}
			}
		} else if (url.contains("mall.jd.com")) {

			Pattern pattern = Pattern.compile("(\\d+)-60.html");
			Matcher matcher = pattern.matcher(url);
			int curPage = 1;
			if (matcher.find()) {
				curPage = Integer.parseInt(matcher.group(1));
			}
			// 控制翻页，以免下一页控制失效，而造成无限翻页。
			// 商品总数
			int total = 0;
			if (resultData.containsKey("total_cnt")) {
				Pattern p = Pattern.compile("(\\d+)");
				Matcher m = p.matcher(resultData.get("total_cnt").toString());
				if (m.find()) {
					total = Integer.parseInt(m.group(1));
				}
			}
			resultData.remove("total_cnt");
			if (total > curPage * 60) {
				Map nextpageTask = new HashMap();
				String sNextpageUrl = url.replace(curPage + "-60.html", (curPage + 1) + "-60.html");
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
