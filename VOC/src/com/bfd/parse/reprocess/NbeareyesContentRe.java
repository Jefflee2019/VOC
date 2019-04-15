package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

public class NbeareyesContentRe implements ReProcessor{
	private static final Pattern PATTIME = Pattern.compile("[0-9]{4}[-年][0-9]{1,2}[-月][0-9]{1,2}[日\\s]*([0-9]{2}:[0-9]{2})*");
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> parseData = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		if(resultData.containsKey(Constants.POST_TIME)){
			String postTime = (String) resultData.get(Constants.POST_TIME);
			Matcher mch = PATTIME.matcher(postTime);
			if(mch.find()){
				postTime = mch.group()
						.replace("年", "-")
						.replace("月", "-")
						.replace("日", "");
				resultData.put(Constants.POST_TIME, postTime);
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, parseData);
	}

}
