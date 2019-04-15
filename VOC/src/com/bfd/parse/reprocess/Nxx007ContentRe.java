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
 * 站点名：中国投资咨询网
 * <p>
 * 主要功能：处理新闻中的多余数据
 * @author bfd_01
 *
 */
public class Nxx007ContentRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if(resultData != null && !resultData.isEmpty()) {
			if (resultData.containsKey(Constants.SOURCE)) {
				String source = resultData.get(Constants.SOURCE).toString();
				source = source.replace("来源：", "").trim();
				resultData.put(Constants.SOURCE, source);
			}
			if (resultData.containsKey(Constants.POST_TIME)) {
				String posttime = resultData.get(Constants.POST_TIME).toString();
				Pattern p = Pattern.compile("(\\d+-\\d+-\\d+)");
				Matcher m = p.matcher(posttime);
				while(m.find()) {
					posttime = m.group(1);
				}
				resultData.put(Constants.POST_TIME, posttime);
			}
		}
		return new ReProcessResult(processcode, processdata);
	}

}
