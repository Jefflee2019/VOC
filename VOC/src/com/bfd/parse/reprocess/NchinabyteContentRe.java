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
 * 站点名：Nchinabyte
 * 
 * 主要功能：处理作者字段
 * 
 * @author bfd_06
 */

public class NchinabyteContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NchinabyteContentRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();

		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			LOG.warn("未获取到解析数据");
			return null;
		}

		// 作者：佚名
		if (resultData.containsKey(Constants.AUTHOR)) {
			String author = resultData.get(Constants.AUTHOR).toString();
			if (author.contains("作者")) {
				author = author.replaceAll("作者\\S", "");
				resultData.put(Constants.AUTHOR, author);
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}