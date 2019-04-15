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
 * 站点名：ITBear科技资讯
 * <p>
 * 主要功能：处理翻页
 * @author bfd_01
 *
 */
public class NitbearListRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NitbearListRe.class);
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			String url = unit.getUrl();
			String newscnt = null;
			int pagesize = 0;
			String nextpage = null;
			int pageno = 0;
			if (resultData.containsKey(Constants.NEWS_CNT)) {
				newscnt = resultData.get(Constants.NEWS_CNT).toString();
			}
			if (newscnt != null && newscnt.contains("/")) {
				pagesize = Integer.valueOf(newscnt.split("/")[1]);
			}
			
			if (!url.contains("page") && pagesize > 1) {
				nextpage = url + "&page=2";
			} else {
				pageno = getPageNo(url);
				if (pageno < pagesize) {
					nextpage = url.split("&page=")[0] + "&page=" + (pageno + 1);
				}
			}
			if (nextpage != null) {
				// 生成下一页任务
				Map<String, Object> nextpageTask = new HashMap<String, Object>();
				nextpageTask.put("link", nextpage);
				nextpageTask.put("rawlink", nextpage);
				nextpageTask.put("linktype", "newslist");
				LOG.info("url:" + url + "taskdata is "
						+ nextpageTask.get("link")
						+ nextpageTask.get("rawlink")
						+ nextpageTask.get("linktype"));
				if (resultData != null && !resultData.isEmpty()) {
					resultData.put("nextpage", nextpage);
					@SuppressWarnings("unchecked")
					List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData
							.get("tasks");
					tasks.add(nextpageTask);
				}
				// 后处理插件加上iid
				ParseUtils.getIid(unit, result);
			}
		}
		return new ReProcessResult(processcode, processdata);
	}
	
	private int getPageNo(String url) {
		Pattern p = Pattern.compile("&page=(\\d+)");
		Matcher m = p.matcher(url);
		while (m.find()) {
			return Integer.valueOf(m.group(1));
		}
		return 0;
	}

}
