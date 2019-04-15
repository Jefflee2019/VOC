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
 * @author bfd_02
 *
 */

public class EbydubaiListRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(EbydubaiListRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();

		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			LOG.warn("未找到解析数据");
			return null;
		}

		/**
		 * @param nextpage
		 * @function 拼接下一页任务
		 */
//		https://www.bydubai.com/pt/catalogsearch/result/index/?p=2&q=Samsung
		String url = unit.getUrl();
		String pageData = unit.getPageData();
		if (pageData.contains("next i-next")) {
			getNextpage(resultData, url);
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	private void getNextpage(Map<String, Object> resultData, String url) {
		Matcher match = Pattern.compile("p=(\\d+)").matcher(url);
		String nextpage = null;
		if (match.find()) {
			int pageindex = Integer.parseInt(match.group(1));
			nextpage = url.replace("p=" + pageindex, "p=" + (pageindex + 1));
		} else {
			nextpage = url.replace("q=", "p=2&q=");
		}

		resultData.put(Constants.NEXTPAGE, nextpage);

		Map<String, Object> nextpageTask = new HashMap<String, Object>();
		nextpageTask.put("link", nextpage);
		nextpageTask.put("rawlink", nextpage);
		nextpageTask.put("linktype", "eclist");

		List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
		tasks.add(nextpageTask);
	}
	
}