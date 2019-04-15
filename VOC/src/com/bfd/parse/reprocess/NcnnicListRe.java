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
 * 站点名：CNNIC
 * <p>
 * 主要功能：处理下一页链接
 * @author bfd_01
 *
 */
public class NcnnicListRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NcnnicListRe.class);
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			int newscnt = 1;
			if (resultData.containsKey(Constants.NEWS_CNT)) {
				String cnt = resultData.get(Constants.NEWS_CNT).toString();
				// 共35条记录 共4页 第1页 首页 | 上一页 1 2 3 4 下一页 尾页
				Pattern p = Pattern.compile("共(\\d+)页");
				Matcher m = p.matcher(cnt);
				while (m.find()) {
					newscnt = Integer.valueOf(m.group(1));
				}
				resultData.remove(Constants.NEWS_CNT);
			}
			
			String url = unit.getUrl();
			int num = 1;
			num = getPageNo(url);
			String nextpage = null;
			if (num < newscnt) {
				nextpage = url.replace("pageNo=" + num, "pageNo=" + (num +1));
			}
			if (nextpage != null) {
				Map<String, Object> nextpageTask = new HashMap<String, Object>();
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
	
	private int getPageNo(String url) {
		Pattern p = Pattern.compile("&pageNo=(\\d+)");
		Matcher m = p.matcher(url);
		int pageNo = 1;
		while (m.find()) {
			pageNo = Integer.valueOf(m.group(1));
		}
		return pageNo;
	}
}
