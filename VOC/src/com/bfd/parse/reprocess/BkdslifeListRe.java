package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 站点名：Bkdslife
 * 
 * 功能：标准化发表时间 加上第一页的下一页
 * 
 * @author bfd_06
 */
public class BkdslifeListRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		/**
		 * 标准化发表时间
		 */
		List<Map<String, Object>> ritems = (List<Map<String, Object>>) resultData
				.get(Constants.ITEMS);
		for (Map<String, Object> ritem : ritems) {
			String posttime = (String) ritem.get(Constants.POSTTIME);
			posttime = "20" + posttime;
			ritem.put(Constants.POSTTIME, posttime);
		}

		return new ReProcessResult(processcode, processdata);
	}

}
