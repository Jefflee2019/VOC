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
 * 站点名：南海网
 * <p>
 * 主要功能：处理新闻内容页字段
 * <p>
 * @author bfd_01
 *
 */
public class NhinewsContentRe implements ReProcessor {
//	private static final Log LOG = LogFactory.getLog(NhinewsContentRe.class);
	private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}\\s+?(\\d{2}:\\d{2}(:\\d{2}?)?)");
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {

		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			
			if (resultData.containsKey(Constants.POST_TIME)) {
				String posttime = resultData.get(Constants.POST_TIME)
						.toString();
				Matcher m = DATE_PATTERN.matcher(posttime);
				if (m.find()) {
					posttime = m.group();
				}
				resultData.put(Constants.POST_TIME, posttime);
			}
			
			if (resultData.containsKey(Constants.SOURCE)
					&& resultData.containsKey(Constants.AUTHOR)) {

				if (resultData.get(Constants.SOURCE).equals(
						resultData.get(Constants.AUTHOR))) {
					String temp = resultData.get(Constants.SOURCE).toString();
					String author = null;
					String source = null;
					if (temp.contains("来源：") && temp.contains("作者：")) {
						if (!temp.endsWith("来源：")) {
							source = temp.split("来源：")[1].split("作者：")[0];
						}
						if (!temp.endsWith("作者：")) {							
							author = temp.split("来源：")[1].split("作者：")[1];
							if (author.contains(" ")) {
								author = author.split(" ")[0];
							}
						} else {
							author = "";
						}

					}
					resultData.put(Constants.SOURCE, source.trim());
					resultData.put(Constants.AUTHOR, author.trim());
				} else {
					resultData.put(Constants.AUTHOR,
							resultData.get(Constants.AUTHOR).toString()
									.replace("作者：", ""));
				}
			}
			
			if (resultData.containsKey(Constants.KEYWORD)) {
				resultData.put(Constants.KEYWORD, resultData.get(Constants.KEYWORD).toString().replace("关键词：", ""));
			}
		}
		return new ReProcessResult(processcode, processdata);
	}
}
