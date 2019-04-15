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

public class Eantel_uruguayListRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(Eantel_uruguayListRe.class);

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
		 * 
		 */
		List<Map<String, Object>> items = (List<Map<String, Object>>) resultData.get("items");
		for (Map<String, Object> map : items) {
			String itemname = (String) map.get("itemname");
			String brand_name = (String) map.get("brand_name");
			map.put("itemname", itemname.replaceAll(brand_name, "").trim());
			map.remove("brand_name");
		}
		
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}