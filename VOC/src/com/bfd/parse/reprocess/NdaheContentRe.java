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
 * 站点名：大河网
 * <p>
 * 主要功能：来源和CATE
 * 
 * @author bfd_01
 *
 */
public class NdaheContentRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			if (resultData.containsKey(Constants.SOURCE)) {
				String source = resultData.get(Constants.SOURCE).toString();
				source = source.replace("来源:", "");
				source = source.replace("来源：", "");
				resultData.put(Constants.SOURCE, source);
			}
			
			//newspaper类发表时间处理
			String url = unit.getUrl();
			if(url.contains("newpaper.dahe")) {
				Matcher match = Pattern.compile("html/(\\S*)/content").matcher(url);
				if(match.find()) {
					String postTime = match.group(1).replace("/", "-");
					resultData.put(Constants.POST_TIME, postTime);
				}
			}
		}
		return new ReProcessResult(processcode, processdata);
	}

}
