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
 * 站点名：Epcfactory_chile
 * 
 * 主要功能：处理下一页
 * 
 * @author lth
 *
 */
public class Epcfactory_chileListRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl();
		List<Map<String, Object>> tasksList = (List<Map<String, Object>>) resultData.get(Constants.TASKS);

		if (resultData.containsKey(Constants.ITEMS)) {
			List<Map<String, Object>> itemsList = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
			// 以每页的商品数判断时候生成下一页任务
			if (itemsList.size() == 50) {
				Map<String, Object> nextpageTask = new HashMap<String, Object>();
				String nextPage = getnextPage(url, "pagina=(\\d+)");
				nextpageTask.put(Constants.LINK, nextPage);
				nextpageTask.put(Constants.RAWLINK, nextPage);
				nextpageTask.put(Constants.LINKTYPE, "eclist");
				resultData.put(Constants.NEXTPAGE, nextPage);
				tasksList.add(nextpageTask);
				resultData.put(Constants.TASKS, tasksList);
			}
		}

		 ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	/**
	 * @param url
	 * @param regex
	 */
	private String getnextPage(String url, String regex) {
		Matcher match = Pattern.compile(regex).matcher(url);
		String nextpage = null;
		if (match.find()) {
			int pageno = Integer.parseInt(match.group(1));
			nextpage = url.replace("pagina=" + pageno, "pagina=" + (pageno + 1));
		} else {
			nextpage = url.concat("?pagina=1");
		}
		return nextpage;
	}

}
