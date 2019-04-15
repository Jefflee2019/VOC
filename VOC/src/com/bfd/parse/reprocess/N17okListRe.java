package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.Constants;
import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site:财界网 (N17ok)
 * @function 新闻列表页后处理插件-处理下一页正常翻页问题
 * 
 * @author bfd_02
 *
 */

public class N17okListRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(N17okListRe.class);

	@SuppressWarnings("unchecked")
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();
		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			LOG.warn("未找到解析数据");
			return null;
		}
		String pageData = unit.getPageData();
		// 列表页url
		// http://www.17ok.com/index2009.php?title=%E5%8D%8E%E4%B8%BA&page=1
		String url = unit.getUrl();
		Matcher match = Pattern.compile("page=(\\d+)").matcher(url);
		if (match.find()) {
			int pagesize = Integer.parseInt(match.group(1));
			if (pageData.contains(">下一页<")) {
				String nextPage = url.replace("page=" + pagesize, "page=" + (pagesize + 1));
				Map<String, Object> nextpageTask = new HashMap<String, Object>();
				nextpageTask.put(Constants.link, nextPage);
				nextpageTask.put(Constants.rawlink, nextPage);
				nextpageTask.put(Constants.linktype, "newslist");

				resultData.put(Constants.nextpage, nextPage);

				List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
				resultData.put(Constants.tasks, tasks);
				tasks.add(nextpageTask);
			}
		} else {
			LOG.warn("The listurl regex failed and the listUrl chosen is wrong");
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
