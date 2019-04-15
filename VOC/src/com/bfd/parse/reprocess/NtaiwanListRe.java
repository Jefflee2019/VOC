package com.bfd.parse.reprocess;

import java.util.ArrayList;
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
 * @site：Ntaiwan
 * @function 列表页后处理插件，deal with 75页的nextpage会循环第一页
 * @author bfd_02
 *
 */
public class NtaiwanListRe implements ReProcessor {

	@SuppressWarnings("rawtypes")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		String url = (String) unit.getTaskdata().get("url");
		Pattern ptn = Pattern.compile("&p=(\\d+)&");
		Matcher m = ptn.matcher(url);
		if(m.find()) {
			int p = Integer.parseInt(m.group(1));
			//p=74 对应的是第75页，下一页会循环到第1页
			if(p >= 74) {
				resultData.remove(Constants.NEXTPAGE);
				List tasks = (ArrayList) resultData.get(Constants.TASKS);
				for (int i = 0; i < tasks.size(); i++) {
					if (((Map) tasks.get(i)).get(Constants.LINKTYPE).equals(
							"newslist")) {
						tasks.remove(i);
					}
				}
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}

}
