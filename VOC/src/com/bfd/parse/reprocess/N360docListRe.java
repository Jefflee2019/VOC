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
 * @site：Neastmoneyblog
 * @function 列表页后处理插件，处理
 * @author bfd_02
 *
 */
public class N360docListRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			String url = unit.getUrl();
			List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
			// https://www.so.com/s?q=%E5%8D%8E%E4%B8%BA+site%3Awww.360doc.com&pn=3&src=srp_paging&adv_t=m&fr=tab_news
			Matcher pnM = Pattern.compile("&pn=(\\d+)").matcher(url);
			if(pnM.find()){
				int pn = Integer.parseInt(pnM.group(1));
				if(pn < 5){
					pn = pn + 1;
					url = url.replaceAll("&pn=(\\d+)", "&pn=" + pn);
					Map<String,Object> link = new HashMap<String,Object>();
					link.put(Constants.LINK, url);
					link.put(Constants.RAWLINK, url);
					link.put(Constants.LINKTYPE, "newslist");
					resultData.put(Constants.NEXTPAGE, url);
					tasks.add(link);
					resultData.put(Constants.TASKS, tasks);
				}
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}
}
