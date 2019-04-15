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
 * 站点名：博客园
 * <p>
 * 主要功能：处理下一页链接
 * @author bfd_01
 *
 */
public class NcnblogsListRe implements ReProcessor {
//	private static final Log LOG = LogFactory.getLog(NcnblogsListRe.class);
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			
			String url = unit.getUrl();
			String data = unit.getPageData();
			String nextpage = null;
			// http://zzk.cnblogs.com/s/news?Keywords=%E5%8D%8E%E4%B8%BA
			// http://zzk.cnblogs.com/s/news?Keywords=%E5%8D%8E%E4%B8%BA&pageindex=2
			int pageindex = 0;
			if (data.contains("Next")) {
				if (!url.contains("pageindex")) {
					nextpage = url + "&pageindex=2";
				} else {
					Pattern p = Pattern.compile("pageindex=(\\d+)");
					Matcher m = p.matcher(url);
					while (m.find()) {
						pageindex = Integer.valueOf(m.group(1));
					}
					nextpage = url.replace("pageindex=" + pageindex,
							"pageindex=" + (pageindex + 1));
				}
			}
			Map<String, String> nextpageTask = new HashMap<String, String>();
			nextpageTask.put("link", nextpage);
			nextpageTask.put("rawlink", nextpage);
			nextpageTask.put("linktype", "newslist");
			resultData.put(Constants.NEXTPAGE, nextpage);
			List<Map> tasks = (List<Map>) resultData.get("tasks");
			tasks.add(nextpageTask);
			ParseUtils.getIid(unit, result);
		}
		return new ReProcessResult(processcode, processdata);
	}
}
