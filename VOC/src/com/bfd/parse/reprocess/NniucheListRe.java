package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.entity.Constants;
import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.reprocess.ReProcessResult;
import com.bfd.parse.reprocess.ReProcessor;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：牛车网
 * 功能：处理翻页
 * @author bfd01
 *
 */
public class NniucheListRe implements ReProcessor {

	private static final Log log = LogFactory.getLog(NniucheListRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;
		
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 得到模板解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		try {
			if (!resultData.isEmpty() && resultData.size() > 0) {
				String url = unit.getUrl();
				String data = unit.getPageData();
				int size = 0;
				int pageIndex = 1;
				if (resultData.containsKey(Constants.ITEMS)) {
					List items = (List) resultData.get(Constants.ITEMS);
					size = items.size();
				}
				if (!url.contains("&p=")) {
					pageIndex = 1;
				} else {
					Pattern p = Pattern.compile("&p=(\\d+)");
					Matcher m = p.matcher(url);
					while (m.find()) {
						pageIndex = Integer.valueOf(m.group(1));
					}
				}
				String nextpage = null;
				if (size == 10 ) {
					if (pageIndex == 1) {
						nextpage = url + "&p=" + pageIndex;
					} else {
						nextpage = url.replace("&p=" + pageIndex, "&p=" + (pageIndex + 1));
					}
				}
				if (nextpage != null) {
					resultData.put(Constants.NEXTPAGE, nextpage);
					Map<String, Object> nextpageTask = new HashMap<String, Object>();
					nextpageTask.put("link", nextpage);
					nextpageTask.put("rawlink", nextpage);
					nextpageTask.put("linktype", "newslist");
					List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData
							.get("tasks");
					tasks.add(nextpageTask);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			log.info( this.getClass().getName() + "reprocess exception...");
			processcode = 1;
		}
		ParseUtils.getIid(unit, result);
		// 解析结果返回值 0代表成功
		return new ReProcessResult(processcode, processdata);
	}
}
