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
 * @site:3g门户 (N3g)
 * @function 处理发表时间、来源字段
 * 
 * @author bfd_02
 *
 */

public class N3gContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(N3gContentRe.class);

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
		 *  来源：驱动之家 2017-06-24 10:19:16
		 */

		if (resultData.containsKey(Constants.SOURCE)) {
			String source = resultData.get(Constants.SOURCE).toString();
			String[] str = source.split(" ");
			if (str[0].contains("来源：")) {
				source = str[0].replace("来源：", "");
			}
			String postTime = str[1]+" "+str[2];
			resultData.put(Constants.SOURCE, source);
			resultData.put(Constants.POST_TIME, postTime);
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}