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

public class EaxiomListRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(EaxiomListRe.class);

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
			String stock = (String) map.get("stock");
			/**
			 * 缺货
			 */
			if (stock != null) {
				map.put("stock", "Y");
			}
			
			Map itemlink = (Map) map.get("itemlink");
			String rawlink = (String) itemlink.get("rawlink");
			String link = (String) itemlink.put("link", "https://www.axiomtelecom.com" + rawlink);
		}
		
		
//		List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
//		for (Map<String, Object> map : tasks) {
//			String rawlink = (String) map.get("rawlink");
//			String link = (String) map.put("link", "https://www.axiomtelecom.com" + rawlink);
//		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}