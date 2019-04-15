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
 * @site:安卓网-新闻(Nhiapk)
 * @function 处理下一页翻页异常
 * @author bfd_02
 *
 */

public class NhiapkListRe implements ReProcessor {
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(NhiapkListRe.class);

	@SuppressWarnings({ "unchecked"})
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();
		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			return null;
		}

		// cal nextpage
		// 获取页面源码
		String pageData = unit.getPageData();
		// 获取当前页url
		// http://zhannei.baidu.com/cse/search?q=%E5%8D%8E%E4%B8%BA&p=74&s=12052907993487091575&srt=lds&nsid=6
		String url = (String) unit.getTaskdata().get("url");
		if (pageData.contains(">下一页")) {
			Pattern ptn = Pattern.compile("&p=(\\d+)");
			Matcher m = ptn.matcher(url);
			if (m.find()) {
				int p = Integer.parseInt(m.group(1));
				String nextPage = url.replace("&p=" + p, "&p=" + (p + 1));
				// task加入下一页任务
				Map<String, Object> nextpageTask = new HashMap<String, Object>();
				nextpageTask.put(Constants.LINK, nextPage);
				nextpageTask.put(Constants.RAWLINK, nextPage);
				nextpageTask.put(Constants.LINKTYPE, "newslist");
				List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
		
				resultData.put(Constants.NEXTPAGE, nextpageTask);
				tasks.add(nextpageTask);
				// p=74对应的是第75页，下一页会循环到第1页
				if (p >= 74) {
					resultData.remove(Constants.NEXTPAGE);
					for (int i = 0; i < tasks.size(); i++) {
						if (((Map<String, Object>) tasks.get(i)).get(Constants.LINKTYPE).equals("newslist")) {
							tasks.remove(i);
						}
					}
				}
			}

		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
