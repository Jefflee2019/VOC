package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：Eripley_peru
 * 
 * 主要功能：处理价格和链接
 * 
 * @author lth
 *
 */
public class Eripley_peruListRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		if (resultData.containsKey(Constants.ITEMS)) {
			List<Map<String, Object>> itemsList = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
			if (itemsList != null && !itemsList.isEmpty()) {
				for (Map<String,Object> itemMap : itemsList) {
					// 处理价格 Internet: S/ 1,199
					if (itemMap.containsKey("itemprice")) {
						String itemprice = itemMap.get("itemprice").toString();
						if (itemprice.contains("Internet")) {
							itemprice = itemprice.replaceAll("Internet:\\s*", "");
							itemMap.put("itemprice", itemprice);
						}
					}
				}
			}
		}
		
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
