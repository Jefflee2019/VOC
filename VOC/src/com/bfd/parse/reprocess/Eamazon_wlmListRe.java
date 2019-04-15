package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：亚马逊(Eamazon_wlm)
 * 
 * 主要功能：控制返回的商品数
 * 
 * @author lth
 *
 */
public class Eamazon_wlmListRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		if (resultData.containsKey(Constants.ITEMS)) {
			// 只处理tasks
			List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
			// 组装限定商品
			List<Map<String, Object>> tasksNew = new ArrayList<Map<String, Object>>();
			if (tasks != null && !tasks.isEmpty()) {
				// 搜索结果数>20个时，只取前20个
				int listSize = tasks.size() >= 20 ? 20 : tasks.size();
				for (int i = 0; i < listSize; i++) {
					Map<String, Object> itemMap = tasks.get(i);
					tasksNew.add(itemMap);
				}
			}
			// 清除旧的商品信息
			resultData.remove(Constants.TASKS);
			// 放入添加限制后的商品信息
			resultData.put(Constants.TASKS, tasksNew);
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
