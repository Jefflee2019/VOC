
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
 * 站点名：数字尾巴(论坛)
 * 
 * 主要功能：
 * 		格式化列表中的回复数
 * 
 * @author bfd_03
 *
 */
public class BdgtleListRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		// 处理列表中的数据
		if (resultData.containsKey(Constants.ITEMS)) {
			stringToMap(resultData);
		}
//		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	@SuppressWarnings("unchecked")
	public void stringToMap(Map<String, Object> resultData) {
		// 列表中的回复数
		List<Map<String,Object>> itemsList = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
		for (int i = 0; itemsList != null & i < itemsList.size(); i++) {
			Map<String,Object> itemsMap = itemsList.get(i);
			if(itemsMap.containsKey(Constants.REPLY_CNT)){
				String sReplyCnt = (String) itemsMap.get(Constants.REPLY_CNT);
				sReplyCnt = sReplyCnt.replace("回复· 查看·", "").trim();
				if(null == sReplyCnt || sReplyCnt.equals("")){
					sReplyCnt = "0";
				}
				itemsMap.put(Constants.REPLY_CNT, sReplyCnt);
			}
			//处理回复时间 "posttime": "2 周前"...
			if(itemsMap.containsKey(Constants.POSTTIME)){
				String posttime = (String) itemsMap.get(Constants.POSTTIME);
				int index = posttime.indexOf("周前");
				if(index > 0){
					int num = Integer.parseInt(posttime.replace("周前", "").trim());
					num = num * 7;
					posttime = num + "天前";
				}
				posttime = ConstantFunc.convertTime(posttime);
				itemsMap.put(Constants.POSTTIME, posttime);
			}
			resultData.put(Constants.ITEMS, itemsList);
		}
		
	}
	
}
