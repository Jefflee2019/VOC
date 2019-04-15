package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：京东商城(Ejd_wlm2)
 * 
 * 主要功能：生成列表页的下一页的链接，限制搜索结果数
 * 
 * @author lth
 *
 */
public class Ejd_wlm2ListRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		//过滤推荐类商品  模板itemimg字段标记的是没有搜索结果时的提示
		if(resultData.containsKey("itemimg")) {
			resultData.put("items", null);
			resultData.put("tasks", null);
			resultData.remove("itemimg");
		}
		
		/**
		 * @function 详情页中取价格，会受反爬影响而被封ip。所以从列表页中获取，拼接到tasks的url中
		 */

		if (resultData.containsKey(Constants.ITEMS)) {
			// 价格从列表页的mysql表关联取得，不再拼接 --2017/10/26
			List<Map<String, Object>> itemsList = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
			List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);

			List<Map<String, Object>> itemsListNew = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> tasksNew = new ArrayList<Map<String, Object>>();
			if (itemsList != null && !itemsList.isEmpty()) {
				int listSize = itemsList.size() >= 20 ? 20 : itemsList.size();
				for (int i = 0; i < listSize; i++) {
					Map<String, Object> itemMap = itemsList.get(i);
					cleanPrice(itemMap);
					itemsListNew.add(itemMap);
					// 预防tasks中有下一页任务出现在第一条
					Map<String, Object> itemlink = (Map<String, Object>) itemMap.get("itemlink");
					String link = itemlink.get("link").toString();
					for (Map<String, Object> taskMap : tasks) {
						String tasklink = taskMap.get("link").toString();
						if (link.equals(tasklink)) {
							tasklink = link.concat("?x=x");
							taskMap.put("link",tasklink);
							taskMap.put("rawlink",tasklink);
							tasksNew.add(taskMap);
							break;
						}
					}
				}
			}
			// 清除旧的商品信息
			resultData.remove(Constants.ITEMS);
			resultData.remove(Constants.TASKS);

			// 放入添加限制后的商品信息
			resultData.put(Constants.ITEMS, itemsListNew);
			resultData.put(Constants.TASKS, tasksNew);
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	/**
	 * @param itemMap
	 */
	private void cleanPrice(Map<String, Object> itemMap) {
		if (itemMap.containsKey("itemprice")) {
			String itemprice = itemMap.get("itemprice").toString();
			Matcher match = Pattern.compile("[\\d\\.]+").matcher(itemprice);
			if (match.find()) {
				itemprice = match.group();
			} else {
				itemprice = "0";
			}
		}
	}
}
