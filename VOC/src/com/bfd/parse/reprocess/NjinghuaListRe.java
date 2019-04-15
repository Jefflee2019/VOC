package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
/**
 * 站点名：京华时报
 * <p>
 * 主要功能：处理下一页
 * @author bfd_01
 *
 */
public class NjinghuaListRe implements ReProcessor {
//	private static final Log LOG = LogFactory.getLog(NjinghuaListRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
//		String next = unit.getPageData();
		if (!unit.getPageData().contains("下一页") && !resultData.isEmpty()) {
			resultData.remove("nextpage");
			List tasks = (List) resultData.get(Constants.TASKS);
			for (int i = 0; i < tasks.size(); i++) {
				Map map = (Map) tasks.get(i);
				if ("newslist".equals(map.get("linktype"))) {
					tasks.remove(i);
					break;
				}
			}
//		if (!resultData.isEmpty() && next.contains("下一页")) {
//			String url = unit.getUrl();
//			// 处理下一页链接
//			Map<String, Object> nextpageTask = new HashMap<String, Object>();
//			String nextpage = getNextpage(url);
//			nextpageTask.put("link", nextpage);
//			nextpageTask.put("rawlink", nextpage);
//			nextpageTask.put("linktype", "newslist");
//			LOG.info("url:" + url + "taskdata is " + nextpageTask.get("link")
//					+ nextpageTask.get("rawlink")
//					+ nextpageTask.get("linktype"));
//			if (!resultData.isEmpty()) {
//				resultData.put("nextpage", nextpage);
//				List<Map<String,Object>> tasks = (List<Map<String,Object>>) resultData.get("tasks");
//				tasks.add(nextpageTask);
//			}
			// 后处理插件加上iid
			ParseUtils.getIid(unit, result);
		}
		return new ReProcessResult(processcode, processdata);
	}

//	private String getPage(String url) {
//		Pattern iidPatter = Pattern.compile("&pn=(\\d+)");
//		Matcher match = iidPatter.matcher(url);
//		while (match.find()) {
//			return match.group(1);
//		}
//		return null;
//	}
//	
//	/**
//	 * 取得下一页链接
//	 * @param url
//	 * @return
//	 */
//	private String getNextpage(String url) {
//		String nextpage = null;
//		if (!url.contains("&pn=")) {
//			if (url.contains("&oq=")) {
//				nextpage = url.split("&oq=")[0] + "&pn=10"
//						+ url.split("&oq=")[1];
//			} else {
//				nextpage = url + "&pn=10";
//			}
//		} else {
//			int pageNum = Integer.valueOf(getPage(url)) + 10;
//			String[] urlArray = url.split("&pn=(\\d+)");
//			if (urlArray.length == 2) {
//				nextpage = url.split("&pn=(\\d+)")[0] + "&pn=" + pageNum
//						+ url.split("&pn=(\\d+)")[1];
//			} else if (urlArray.length == 1) {
//				nextpage = url.split("&pn=(\\d+)")[0] + "&pn=" + pageNum;
//			}
//		}
//		return nextpage;
//	}
}
