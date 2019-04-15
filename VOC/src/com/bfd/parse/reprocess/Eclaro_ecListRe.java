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
 * 站点名：Eclaro_ec
 * 
 * 主要功能：合并商品品牌和名称(名称不带品牌，不能识别品牌)
 * 
 * @author lth
 *
 */
public class Eclaro_ecListRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		if (resultData.containsKey(Constants.ITEMS)) {
			List<Map<String, Object>> itemsList = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
			for (Map<String, Object> itemMap : itemsList) {
				if (itemMap.containsKey("itemname") && itemMap.containsKey("brand")) {
					String brand = itemMap.get("brand").toString();
					String itemname = new StringBuffer().append(brand).append(" ").append(itemMap.get("itemname"))
							.toString();
					itemMap.put("itemname", itemname);
					itemMap.remove("brand");
				}
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
