package com.bfd.parse.reprocess;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：北京青年报 主要功能：处理取到的数据，内容，来源等，过滤不需要的内容，
 * 
 * @author bfd_01
 *
 */
public class NynetContentRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			if (resultData.containsKey(Constants.POST_TIME)) {
				String postTime = resultData.get(Constants.POST_TIME).toString();
				// 处理发表时间不带年份的情况 "06-11 12:22"
				Matcher match = Pattern.compile("(?:(\\d{2,4}).)?(\\d+).(\\d+).\\s*(\\d+):(\\d+)(?:\\:(\\d+))?")
						.matcher(postTime);
				if (match.find()) {
					Calendar calendar = Calendar.getInstance();
					int year = calendar.get(Calendar.YEAR);
					postTime = year + "-" + match.group();
					resultData.put(Constants.POST_TIME, postTime);
				}
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}
}
