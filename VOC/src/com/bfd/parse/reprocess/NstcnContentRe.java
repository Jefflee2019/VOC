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
 * 站点名：证券时报
 * <p>
 * 主要功能：处理新闻内容页字段
 * <p>
 * @author bfd_01
 *
 */
public class NstcnContentRe implements ReProcessor {
	// private static final Log LOG = LogFactory.getLog(NstcnContentRe.class);
	private static final Pattern DATE_PATTERN = Pattern
			.compile("\\d{4}-\\d{2}-\\d{2}\\s+?(\\d{2}:\\d{2}(:\\d{2}?)?)|\\d{4}-\\d{2}-\\d{2}");

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {

		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {

			if (resultData.containsKey(Constants.SOURCE)
					&& resultData.containsKey(Constants.POST_TIME)) {
				String source = null;
				if (resultData.get(Constants.SOURCE).equals(
						resultData.get(Constants.POST_TIME))) {
					String temp = resultData.get(Constants.SOURCE).toString();
					if (temp.contains("来源：") && temp.split("来源：").length > 1) {
						source = temp.split("来源：")[1];
					}

				} else {
					source = resultData.get(Constants.SOURCE).toString();
					source = source.replace("来源：", "");
					String author = null;
					if (resultData.containsKey(Constants.AUTHOR)
							&& resultData.get(Constants.AUTHOR).toString()
									.contains("作者：")
							&& (!resultData.get(Constants.AUTHOR).toString()
									.endsWith("作者："))) {
						author = resultData.get(Constants.AUTHOR).toString();
						author = author.split("作者：")[1];
					} else {
						author = "";
					}
					resultData.put(Constants.AUTHOR, author);
				}
				resultData.put(Constants.SOURCE, source.trim());
			}

			if (resultData.containsKey(Constants.POST_TIME)) {
				String posttime = resultData.get(Constants.POST_TIME)
						.toString();
				Matcher m = DATE_PATTERN.matcher(posttime);
				if (m.find()) {
					posttime = m.group();
				}
				resultData.put(Constants.POST_TIME, posttime);
			}
		}
		return new ReProcessResult(processcode, processdata);
	}
}
