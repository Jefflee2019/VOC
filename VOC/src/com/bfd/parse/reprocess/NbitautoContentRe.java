package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site:易车网 (Nbitauto)
 * @function 处理发表时间、来源字段
 * 
 * @author bfd_02
 *
 */

public class NbitautoContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NbitautoContentRe.class);

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
		 * @function 清洗字段
		 *  2016-10-15 08:18 来源：新浪汽车
		 */

		if (resultData.containsKey(Constants.SOURCE)) {
			String source = resultData.get(Constants.SOURCE).toString();
			String postTime = "";
			if (source.contains("来源：")) {
				int index = source.indexOf("来源：");
				postTime = source.substring(0, index).trim();
				source = source.substring(index + 3);
			}
			resultData.put(Constants.SOURCE, source);
			resultData.put(Constants.POST_TIME, postTime);
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}