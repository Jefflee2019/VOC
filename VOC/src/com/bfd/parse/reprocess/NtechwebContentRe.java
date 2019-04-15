package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

public class NtechwebContentRe implements ReProcessor {

	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData.containsKey(Constants.AUTHOR)) {
			String author = (String) resultData.get(Constants.AUTHOR);
			resultData.put(Constants.AUTHOR,
					author.replace("作者:", "").replace("责任编辑：", "").replace("(", "").replace(")", ""));
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}
}
