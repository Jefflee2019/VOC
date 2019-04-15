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
 * @site ：新民网(Nxinmin)
 * @function：处理下一页
 * @author bfd_04
 *
 */
public class NxinminListRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NxinminListRe.class);
	private static final Pattern PATTERN = Pattern.compile("p=(\\d+)");
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		
		if (resultData != null && !resultData.isEmpty()) {
			String url = unit.getUrl();
			
			boolean hasNext = unit.getPageData().contains("class=\"unselect number\">下一页</a>");
			if(hasNext) {
				// 处理下一页链接
				Map<String, Object> nextpageTask = new HashMap<String, Object>();
				if (! url.contains("p=")) {
					url = url + "&p=1";
					LOG.debug("fix url");
				}
				Matcher match = PATTERN.matcher(url);
				
				String pageNum = "";
				if(match.find()) {
					pageNum = match.group(1);
				}
				String nextpage = url.replace("p=" + match.group(1), "p=" + 
						String.valueOf((Integer.parseInt(pageNum) + 1))); 
				nextpageTask.put("link", nextpage);
				nextpageTask.put("rawlink", nextpage);
				nextpageTask.put("linktype", "newslist");
				resultData.put("nextpage", nextpage);
				List<Map> tasks = (List<Map>) resultData.get("tasks");
				tasks.add(nextpageTask);
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}
}
