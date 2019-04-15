package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site:360doc-新闻 (N360doc)
 * @function 新闻内容页后处理插件
 * 
 * @author bfd_02
 *
 */

public class N360docContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(N360docContentRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();

		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			LOG.warn("未找到解析数据");
			return null;
		}

		if(resultData.containsKey(Constants.POST_TIME)) {
			String postTime = resultData.get(Constants.POST_TIME).toString();
			Matcher match = Pattern.compile("\\d+\\S\\d+\\S\\d+").matcher(postTime);
			if(match.find()) {
				postTime = match.group();
				resultData.put(Constants.POST_TIME, postTime);
			}
		}
		
		if(resultData.containsKey(Constants.AUTHOR)) {
			String author = resultData.get(Constants.AUTHOR).toString();
			if(author.contains("作者：")) {
				Matcher match = Pattern.compile("作者：(\\S*)\\s*").matcher(author);
				if(match.find()) {
					author = match.group(1);
					resultData.put(Constants.AUTHOR, author);
				}
			}else {
				resultData.remove(Constants.AUTHOR);
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}