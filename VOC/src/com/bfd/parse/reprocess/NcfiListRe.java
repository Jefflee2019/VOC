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
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
/**
 * @site：中财网
 * @function：处理下一页
 * @author bfd_04
 *
 */
public class NcfiListRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NcfiListRe.class);
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		String pageData = unit.getPageData();
		if (resultData != null && !resultData.isEmpty() && pageData.contains("下一页")) {
			String url = unit.getUrl();
			String oldPageNum = getPage(url);
			if (url.contains("baidu.com") && null != oldPageNum) {
				// 处理下一页链接
				Map<String, Object> nextpageTask = new HashMap<String, Object>();
				
				int pageNum = Integer.valueOf(oldPageNum) + 20;
//				String nextpage = URL_HEAD_BD + String.valueOf(pageNum) + URL_END_BD;
				String nextpage = url.replace("&pn=" + oldPageNum, "&pn=" + pageNum);
				nextpageTask.put("link", nextpage);
				nextpageTask.put("rawlink", nextpage);
				nextpageTask.put("linktype", "newslist");
				LOG.debug("url:" + url + "taskdata is " + nextpageTask.get("link")
						+ nextpageTask.get("rawlink")
						+ nextpageTask.get("linktype"));
				resultData.put("nextpage", nextpage);
				List<Map> tasks = (List<Map>) resultData.get("tasks");
				tasks.add(nextpageTask);
			} else if (url.contains("chinaso") && null != oldPageNum) {
				// 处理下一页链接
				Map<String, Object> nextpageTask = new HashMap<String, Object>();

				int pageNum = Integer.valueOf(oldPageNum) + 1;
//				String nextpage = URL_HEAD_BD + String.valueOf(pageNum) + URL_END_BD;
				String nextpage = url.replace("&page=" + oldPageNum, "&page=" + pageNum);
				nextpageTask.put("link", nextpage);
				nextpageTask.put("rawlink", nextpage);
				nextpageTask.put("linktype", "newslist");
				LOG.debug("url:" + url + "taskdata is " + nextpageTask.get("link")
						+ nextpageTask.get("rawlink")
						+ nextpageTask.get("linktype"));
				resultData.put("nextpage", nextpage);
				List<Map> tasks = (List<Map>) resultData.get("tasks");
				tasks.add(nextpageTask);
			}
			
			// 后处理插件加上iid
			ParseUtils.getIid(unit, result);
		}
		return new ReProcessResult(processcode, processdata);
	}
	private String getPage(String url) {
		if(url.contains("baidu.com")) {
			Pattern iidPatter = Pattern.compile("&pn=(\\d+)&");
			Matcher match = iidPatter.matcher(url);
			while (match.find()) {
				return match.group(1);
			}
		} else if(url.contains("chinaso")){
			Pattern iidPatter = Pattern.compile("&page=(\\d+)");
			Matcher match = iidPatter.matcher(url);
			while (match.find()) {
				return match.group(1);
			}
		}
		return null;
	}
}
