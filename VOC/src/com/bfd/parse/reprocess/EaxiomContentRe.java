package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @author bfd_02
 *
 */

public class EaxiomContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(EaxiomContentRe.class);

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
		resultData.put("region", "MEA");
		/**
		 * 添加国家信息
		 */
		resultData.put("country", "AE");
		/**
		 * 缺货
		 */
		if ("Out Of Stock".equals(resultData.get("stock"))) {
			resultData.put("stock", "Y");
		} else {
			resultData.remove("stock");
		}
		
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}