package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site：Npconline
 * @function 手动对详情页url做消重处理
 * @author bfd_02
 *
 */
public class NpconlineListRe implements ReProcessor {

	@SuppressWarnings({ "unchecked" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		if(resultData.containsKey(Constants.TASKS)) {
			List<Map<String,Object>> tasks = (List<Map<String,Object>>) resultData.get(Constants.TASKS);
			for(int i = 0;tasks != null && i <tasks.size();i++) {
				Map<String,Object> taskMap = tasks.get(i);
				if(taskMap.get(Constants.LINKTYPE).equals("newscontent")) {
					String link = taskMap.get(Constants.LINK).toString().replaceAll("\\?.*", "");
					taskMap.put(Constants.LINK, link);
					taskMap.put(Constants.RAWLINK, link);
				}
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}

}
