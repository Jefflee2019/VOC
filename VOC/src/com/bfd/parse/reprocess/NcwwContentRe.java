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

/**
 * 站点名：通信世界网
 * <p>
 * 主要功能：过滤数据中的多余字段
 * 
 * @author bfd_01
 *
 */
public class NcwwContentRe implements ReProcessor {
	// private static final Log LOG = LogFactory.getLog(NcwwContentRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty() && resultData.containsKey(Constants.POST_TIME)) {
			// 为了兼容多个模板，author、post_time、source标定在同一字符串，插件中再清洗
			// 责任编辑：卞海川 2018.06.06 10:05 来源：PingWest
			// 作者：方园婧 责任编辑：魏慧
			String resource = resultData.get(Constants.POST_TIME).toString();
			String postTimeRex = "(\\d{4}\\.\\d{1,2}\\.\\d{1,2}\\s*\\d{1,2}\\:\\d{1,2})";
			String sourceRex = "来源\\S(\\S*)";
			String authorRex = "作者\\S(\\S*)";
			String authorRex2 = "责任编辑\\S(\\S*)";
			String postTime = getRex(resource, postTimeRex);
			resultData.put(Constants.POST_TIME, postTime.replace(".", "-"));
			String source = getRex(resource, sourceRex);
			resultData.put(Constants.SOURCE, source);
			if (resource.contains("作者")) {
				String author = getRex(resource, authorRex);
				resultData.put(Constants.AUTHOR, author);
			} else if (resource.contains("责任编辑")) {
				String author = getRex(resource, authorRex2);
				resultData.put(Constants.AUTHOR, author);
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}

	/**
	 * @param postTime
	 * @param postTimeRex
	 */
	private String getRex(String source, String Rex) {
		String result = null;
		Matcher match = Pattern.compile(Rex).matcher(source);
		if (match.find()) {
			result = match.group(1);
		}
		return result;
	}
}
