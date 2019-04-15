package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 站点名：机锋网(论坛)
 * 
 * 主要功能： 格式化列表中的发表时间
 * 
 * 
 * @author bfd_03
 * 
 */
public class BgfanListRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		// 列表中的回复数
		if (resultData.containsKey(Constants.ITEMS)) {
			stringToMap(resultData, Constants.ITEMS);
		}
		// 限制列表页只翻5页
		Matcher matcher = Pattern.compile("forum-\\d+-(\\d+).html").matcher(
				unit.getUrl());
		if (matcher.find()) {
			int pageNumber = Integer.parseInt(matcher.group(1));
			if (pageNumber > 5 && resultData.containsKey(Constants.NEXTPAGE)){
				resultData.remove(Constants.NEXTPAGE);
			    ((List<Map<String, Object>>)resultData.get(Constants.TASKS)).remove(0);
			}
		}

		return new ReProcessResult(SUCCESS, processdata);
	}

	@SuppressWarnings("unchecked")
	public void stringToMap(Map<String, Object> resultData, String key) {
		// 列表中的回复数
		List<Map<String, Object>> itemsList = (List<Map<String, Object>>) resultData
				.get(Constants.ITEMS);

		for (int i = 0; itemsList != null & i < itemsList.size(); i++) {
			Map<String, Object> itemsMap = itemsList.get(i);
			// 处理发表时间
			if (itemsMap.containsKey(Constants.POSTTIME)) {
				String sPosttime = (String) itemsMap.get(Constants.POSTTIME);
				sPosttime = ConstantFunc.convertTime(sPosttime);
				itemsMap.put(Constants.POSTTIME, sPosttime);
			}
			resultData.put(Constants.ITEMS, itemsList);

		}
	}

}
