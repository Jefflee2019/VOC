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
 * 站点名：太平洋电脑网
 * <p>
 * 主要功能：处理下一页
 * @author bfd_01
 *
 */
public class BpconlineListRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(BpconlineListRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {

		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (unit.getPageData().contains("下一页") && !resultData.isEmpty()) {
			String url = unit.getUrl();

			// 处理下一页链接
			Map<String, Object> nextpageTask = new HashMap<String, Object>();

			String nextpage = getNextpage(url);
			nextpageTask.put("link", nextpage);
			nextpageTask.put("rawlink", nextpage);
			nextpageTask.put("linktype", "bbspostlist");
			LOG.info("url:" + url + "taskdata is " + nextpageTask.get("link")
					+ nextpageTask.get("rawlink")
					+ nextpageTask.get("linktype"));
			if (!resultData.isEmpty()) {
				resultData.put("nextpage", nextpage);
				List<Map<String,Object>> tasks = (List<Map<String,Object>>) resultData.get("tasks");
				tasks.add(nextpageTask);
			}
			// 后处理插件加上iid
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}

	/**
	 * 取得下一页链接
	 * 
	 * @param url
	 * @return
	 */
	private String getNextpage(String url) {
		String nextpage = null;
		if (!url.contains("_")) {
			nextpage = url.split(".html")[0] + "_2" + ".html";
		} else {
			int pageNum = Integer.valueOf(getPage(url)) + 1;
			nextpage = url.split("_")[0] + "_" + pageNum + ".html";
		}
		return nextpage;
	}

	private String getPage(String url) {
		Pattern iidPatter = Pattern.compile("_(\\d+).html");
		Matcher match = iidPatter.matcher(url);
		while (match.find()) {
			return match.group(1);
		}
		return null;
	}
}
