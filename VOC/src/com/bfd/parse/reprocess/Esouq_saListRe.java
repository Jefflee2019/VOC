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
 * cid：Esouq_sa 
 * 主要功能：处理下一页
 * 
 * @author lth
 *
 */
public class Esouq_saListRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData.containsKey("pagecount")) {
			List<Map<String, Object>> items = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
			/**
			 * 处理下一页翻页
			 * 当前页的商品数
			 * 每页60个商品，如果等于60，提供翻页。(存在只有59商品的情况)-2019-01-12 
			 * (太坑了！)商品数经常变动，58、59、60还可能是其它。改成不在按当页商品数量控制翻页
			 * ，通过商品总数判断  -2019-01-15
			 */ 
			String url = unit.getUrl();
			Matcher countmatch = Pattern.compile("\\d+").matcher(resultData.get("pagecount").toString());
			// 商品总数
			Double count = null;
			if(countmatch.find()) {
				count = Double.parseDouble(countmatch.group());
			}
			String pagenoRegex = "page=(\\d+)";
			Matcher pagenomatch = Pattern.compile(pagenoRegex).matcher(url);
			// 当前页码
			int pageno = 1;
			if(pagenomatch.find()) {
				pageno = Integer.parseInt(pagenomatch.group(1));
			}
			if (items != null && items.size() >55 && count/items.size() > pageno) {
				List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
				String nextpage = getRegex(pagenoRegex, url);
				Map<String, Object> nextpageTask = new HashMap<String, Object>();
				nextpageTask.put(Constants.LINK, nextpage);
				nextpageTask.put(Constants.RAWLINK, nextpage);
				nextpageTask.put(Constants.LINKTYPE, "eclist");
				resultData.put(Constants.NEXTPAGE, nextpage);
				tasks.add(nextpageTask);
			}
			resultData.remove("pagecount");
		}
		
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);

	}

	private String getRegex(String regex, String url) {
		Matcher match = Pattern.compile(regex).matcher(url);
		String nextpage;
		if (match.find()) {
			int pageno = Integer.parseInt(match.group(1));
			nextpage = url.replace("page=" + pageno, "page=" + (pageno + 1));
		} else {
			nextpage = url.concat("page=2");
		}
		return nextpage;
	}
}