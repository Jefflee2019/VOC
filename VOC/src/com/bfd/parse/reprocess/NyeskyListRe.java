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
 * 站点名：天极网-新闻
 * <p>
 * 主要功能：处理下一页
 * @author bfd_01
 *
 */
public class NyeskyListRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NyeskyListRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		
		if (resultData.containsKey(Constants.POSTTIME)) {
			String url = unit.getUrl();
			int searchCnt = Integer.valueOf(getSearchCnt(resultData.get(
					Constants.POSTTIME).toString()));
			int pageNum = (searchCnt % 20 == 0) ? searchCnt / 20
					: (searchCnt / 20) + 1;
			int pageNow = Integer.valueOf(getPage(url));
			if (pageNow < pageNum) {
				// 处理下一页链接
				Map<String, Object> nextpageTask = new HashMap<String, Object>();
				String nextpage = getNextpage(url);
				nextpageTask.put("link", nextpage);
				nextpageTask.put("rawlink", nextpage);
				nextpageTask.put("linktype", "newslist");
				LOG.info("url:" + url + "taskdata is "
						+ nextpageTask.get("link")
						+ nextpageTask.get("rawlink")
						+ nextpageTask.get("linktype"));
				if (!resultData.isEmpty()) {
					resultData.put("nextpage", nextpage);
					List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData
							.get("tasks");
					tasks.add(nextpageTask);
				}
				// 后处理插件加上iid
				ParseUtils.getIid(unit, result);
			}
			resultData.remove(Constants.POSTTIME);
		}
		
		return new ReProcessResult(processcode, processdata);
	}

	private String getPage(String url) {
		Pattern iidPatter = Pattern.compile("pageNo=(\\d+)");
		Matcher match = iidPatter.matcher(url);
		while (match.find()) {
			return match.group(1);
		}
		return null;
	}
	private String getSearchCnt(String data) {
		Pattern iidPatter = Pattern.compile("找到相关网页约(\\d+)条，");
		Matcher match = iidPatter.matcher(data);
		while (match.find()) {
			return match.group(1);
		}
		return null;
	}
	
	/**
	 * 取得下一页链接
	 * @param url
	 * @return
	 */
	private String getNextpage(String url) {
		String head = url.split("pageNo=(\\d+)")[0];
		String end = url.split("pageNo=(\\d+)")[1];
		String nextpage = head + "pageNo=" + (Integer.valueOf(getPage(url)) + 1) + end;
		return nextpage;
	}
}
