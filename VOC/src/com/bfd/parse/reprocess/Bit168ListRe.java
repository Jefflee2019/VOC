package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 站点名：it168-论坛
 * 
 * 主要功能：
 * 		格式化列表中的发表时间
 * 
 * @author bfd_03
 *
 */
public class Bit168ListRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		// 列表中的回复数
		if (resultData.containsKey(Constants.ITEMS)) {
			stringToMap(resultData, Constants.ITEMS);
		}
		return new ReProcessResult(SUCCESS, processdata);
	}

	@SuppressWarnings("unchecked")
	public void stringToMap(Map<String, Object> resultData, String key) {
		// 列表中的回复数
		if(key.equals(Constants.ITEMS)){
			List<Map<String,Object>> itemsList = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
			for (int i = 0; itemsList != null & i < itemsList.size(); i++) {
				Map<String,Object> itemsMap = itemsList.get(i);
				if(itemsMap.containsKey(Constants.POSTTIME)){
					String sPosttime = (String) itemsMap.get(Constants.POSTTIME);
					if(sPosttime.contains("发表")){
						sPosttime = sPosttime.replace("发表", "").trim();
					}
					sPosttime = ConstantFunc.convertTime(sPosttime);
					itemsMap.put(Constants.POSTTIME, sPosttime);
				}
				resultData.put(Constants.ITEMS, itemsList);
			}
		}
		
	}
	
}
