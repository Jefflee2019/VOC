package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site:创业邦 (Ncyzone)
 * @function 内容页源码提取发表时间
 * 
 * @author bfd_02
 *
 */

public class NcyzoneContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NcyzoneContentRe.class);

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

		/**
		 * @param post_time
		 * @function 源码提取发表时间
		 */

		String pageData = unit.getPageData();
		if (resultData.containsKey(Constants.POST_TIME)&&pageData.contains("data-time=")) {
			Matcher match = Pattern.compile("data-time=\"(\\d+)\"").matcher(pageData);
			if(match.find()) {
				String postTime = match.group(1);
				resultData.put(Constants.POST_TIME, ConstantFunc.convertTime(postTime));
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}