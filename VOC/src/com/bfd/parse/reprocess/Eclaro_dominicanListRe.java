package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * Eclaro_dominican 主要功能：处理价格
 * 
 * @author lth
 *
 */
public class Eclaro_dominicanListRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		if (resultData.containsKey("items")) {
			List<Map<String, Object>> itemsList = (List<Map<String, Object>>) resultData.get("items");
			if (itemsList != null && !itemsList.isEmpty()) {
				for (Map<String, Object> itemMap : itemsList) {
					// 价格
					if (itemMap.containsKey("itemprice")) {
						String itemprice = itemMap.get("itemprice").toString();
						if(itemprice.equals("No disponible") || itemprice.equals("Gratis")) {
							itemprice = "0";	
						}
						itemMap.put("itemprice", itemprice);
					}
				}
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}