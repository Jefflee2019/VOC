package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
/**
 * 站点名：中国江苏网
 * <p>
 * 主要功能：处理新闻中的多余数据
 * @author bfd_01
 *
 */
public class NjschinaContentRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			if (resultData.containsKey(Constants.SOURCE)) {
				String source = resultData.get(Constants.SOURCE).toString();
				if (source.contains("来源：")) {
					source = source.split("来源：")[1].split(" ")[0];
					resultData.put(Constants.SOURCE, source);
				}
			}
			if (resultData.containsKey(Constants.AUTHOR)) {
				String author = resultData.get(Constants.AUTHOR).toString();
				if (author.contains("作者：")) {
					author = author.replace("作者：", "");
					resultData.put(Constants.AUTHOR, author);
				}
			}
		}
		return new ReProcessResult(processcode, processdata);
	}

}
