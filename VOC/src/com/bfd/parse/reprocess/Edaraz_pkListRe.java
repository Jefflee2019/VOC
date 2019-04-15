package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSONObject;
import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：Edaraz_pk
 * 
 * 主要功能：解析js代码提取商品数据及下一页
 * 
 * @author lth
 *
 */
public class Edaraz_pkListRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		// 页面模板后的源码
		String pageData = unit.getPageData();
		if (!pageData.equals("") && pageData.contains("<script>window.pageData=")) {
			String regex = "<script>window.pageData=(.*)?</script>";
			Matcher match = Pattern.compile(regex).matcher(pageData);
			if (match.find()) {
				String jsonData = match.group(1);
				Map<String, Object> jsonMap = JSONObject.parseObject(jsonData);
				if (jsonMap.containsKey("mods")) {
					Map<String, Object> modsMap = (Map<String, Object>) jsonMap.get("mods");
					if (modsMap.containsKey("listItems")) {
						List<Map<String, Object>> listItems = (List<Map<String, Object>>) modsMap.get("listItems");
						if (listItems != null && !listItems.isEmpty()) {
							List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
							List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
							for (Map<String, Object> listItem : listItems) {
								// 存放单个商品数据
								Map<String, Object> itemMap = new HashMap<String, Object>();
								// 商品名称
								if (listItem.containsKey("name")) {
									String itemname = listItem.get("name").toString();
									itemMap.put("itemname", itemname);
								}
								// 商品链接
								String link = null;
								if (listItem.containsKey("productUrl")) {
									link = listItem.get("productUrl").toString();
								}
								// 存放商品链接属性
								Map<String, Object> itemlink = new HashMap<String, Object>();
								itemlink.put("link", "https"+link);
								itemlink.put("rawlink", "https"+link);
								itemlink.put("linktype", "eccontent");
								itemMap.put("itemlink", itemlink);

								// 商品价格
								if (listItem.containsKey("priceShow")) {
									String itemprice = listItem.get("priceShow").toString();
									itemMap.put("itemprice", itemprice);
								}
								items.add(itemMap);
							}
							resultData.put(Constants.ITEMS, items);
							
							String url = unit.getUrl();
							// 下一页任务
							Map<String,Object> nextpageTask = new HashMap<String, Object>();
							if(items.size() == 40) {
							String nextpage = getRex("page=(\\d+)", url);
							nextpageTask.put("link", nextpage);
							nextpageTask.put("rawlink", nextpage);
							nextpageTask.put("linktype", "eclist");
							tasks.add(nextpageTask);
							resultData.put(Constants.NEXTPAGE, nextpage);
							resultData.put(Constants.TASKS, tasks);
							}
						}
					}
				}
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
	
	public String getRex(String rex,String url) {
		Matcher match = Pattern.compile(rex).matcher(url);
		String nextpage = null;
		if(match.find()) {
			int pageno = Integer.parseInt(match.group(1));
			nextpage = url.replace("page="+pageno, "page="+(pageno+1));
		}else {
			nextpage = url.concat("?page=2");
		}
		return nextpage;
	}

}
