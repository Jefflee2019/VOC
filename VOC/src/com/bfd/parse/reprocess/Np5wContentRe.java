package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @sie:全景网 (Np5w)
 * @function 新闻内容页后处理插件
 * 
 * @author bfd_02
 *
 */

public class Np5wContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(Np5wContentRe.class);

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
		 * @param post_time发表时间
		 * @function 格式化post_time "05月18日 06:12"
		 * 
		 */

		if (resultData.containsKey(Constants.POST_TIME)) {
			String postTime = (String) resultData.get(Constants.POST_TIME);
			postTime = ConstantFunc.convertTime(postTime);
			resultData.put(Constants.POST_TIME, postTime);
		}

		 ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}