package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
/**
 * 站点名：证券之星
 * <p>
 * 主要功能：处理新闻中的多余数据
 * @author bfd_01
 *
 */
public class NstockstarContentRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if(resultData != null && !resultData.isEmpty()) {
			if(resultData.containsKey(Constants.POST_TIME)) {
				String post_time = resultData.get(Constants.POST_TIME).toString();
				Pattern p = Pattern.compile("\\d+-\\d+-\\d+ \\d+:\\d+:\\d+");
				Matcher m = p.matcher(post_time);
				while(m.find()) {
					post_time = m.group();
					resultData.put(Constants.POST_TIME, post_time);
				}
			}
		}
		return new ReProcessResult(processcode, processdata);
	}

}
