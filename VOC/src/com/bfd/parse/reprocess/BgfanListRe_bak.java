package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 站点名：机锋网(论坛)
 * 
 * 主要功能：
 * 		格式化列表中的发表时间
 *		
 * 
 * @author bfd_03
 *
 */
public class BgfanListRe_bak implements ReProcessor {

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
		List<Map<String,Object>> itemsList = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
		List<Map<String,String>> tasksList = (ArrayList<Map<String,String>>) resultData.get(Constants.TASKS);
		
		for (int i = 0; itemsList != null & i < itemsList.size(); i++) {
			Map<String, Object> itemsMap = itemsList.get(i);
			Map<String, String> linkMap = (Map<String, String>) itemsMap.get(Constants.ITEMLINK);
			
			// 处理发表时间
			if (itemsMap.containsKey(Constants.POSTTIME)) {
				String sPosttime = (String) itemsMap.get(Constants.POSTTIME);
				sPosttime = ConstantFunc.convertTime(sPosttime);
				itemsMap.put(Constants.POSTTIME, sPosttime);
			}
			// 处理
			String sLink = linkMap.get(Constants.LINK);
			int index = sLink.indexOf("&extra=page");
			if (index > 0) {
				sLink = sLink.substring(0, index);
				linkMap.put(Constants.LINK, sLink);
				linkMap.put(Constants.RAWLINK, sLink);
			}
			
			resultData.put(Constants.ITEMS, itemsList);
			
		}
		
		for (int i = 0; tasksList != null && i < tasksList.size(); i++) {
			Map<String, String> tasksMap = tasksList.get(i);
			String sLink = tasksMap.get(Constants.LINK);
			String sLinktype = tasksMap.get(Constants.LINKTYPE);
			if(!sLinktype.equals("bbspost")){
				continue;
			}
			int index = sLink.indexOf("&extra=page");
			if (index > 0) {
				sLink = sLink.substring(0, index);
				tasksMap.put(Constants.LINK, sLink);
				tasksMap.put(Constants.RAWLINK, sLink);
			}
			
		}
	}
	
}
