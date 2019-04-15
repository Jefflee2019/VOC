package com.bfd.parse.reprocess;

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
 * Eclaro_colombia 主要功能：处理价格和下一页
 * 
 * @author lth
 *
 */
public class Eclaro_colombiaListRe implements ReProcessor {

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
						if (itemprice.contains("Precio Ahora")) {
							itemprice = itemprice.replace("Precio Ahora", "").trim();
						}
						itemMap.put("itemprice", itemprice);
					}
				}
			}
			// 处理下一页。每页24个商品，满足24个，就提供翻页
			if (itemsList.size() == 24) {
				String url = unit.getUrl();
				List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
				String nextpage = getNextpage(url);
				resultData.put(Constants.NEXTPAGE, nextpage);
				Map<String, Object> nextpageMap = new HashMap<String, Object>();
				nextpageMap.put("link", nextpage);
				nextpageMap.put("rawlink", nextpage);
				nextpageMap.put("linktype", "eclist");
				tasks.add(nextpageMap);
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	/**
	 * @param url
	 */
	private String getNextpage(String url) {
		String nextpage = null;
		Matcher match = Pattern.compile("&beginIndex=(\\d+)&").matcher(url);
		if (match.find()) {
			nextpage = url.replace("&beginIndex=" + match.group(1), "&beginIndex="
					+ (Integer.parseInt(match.group(1)) + 24));

		}
		return nextpage;
	}
}