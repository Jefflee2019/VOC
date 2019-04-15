package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * Eclaro_peru 主要功能：处理价格
 * 
 * @author lth
 *
 */
public class Eliverpool_mexicoListRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		if (resultData.containsKey("items")) {
			List<Map<String, Object>> itemsList = (List<Map<String, Object>>) resultData.get("items");
			if (itemsList != null && !itemsList.isEmpty()) {
				for (Map<String, Object> itemMap : itemsList) {
					// 价格格式化    Precio Lista: $ 89900
					if (itemMap.containsKey("itemprice")) {
						String itemprice = itemMap.get("itemprice").toString()
								           .replaceAll("Precio Lista:\\s*", "");
						// 价格后两位表示指数，应去掉
						itemprice = itemprice.substring(0, itemprice.length()-2);
						itemMap.put("itemprice", itemprice);
					}
				}
			}
		}

//		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}