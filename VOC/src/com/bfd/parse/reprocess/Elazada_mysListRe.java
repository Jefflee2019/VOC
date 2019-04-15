package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;

/**
 * @ClassName: Elazada_mysListRe
 * @author: taihua.li
 * @date: 2019年4月9日 上午11:58:09
 * @Description:源码提取商品相关数据及下一页任务
 */
public class Elazada_mysListRe implements ReProcessor {

	private static final Log LOG = LogFactory.getLog(Elazada_mysListRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		String url = unit.getUrl();

		String pageData = unit.getPageData();
		Matcher match = Pattern.compile(">window.pageData=(.*?)</script>").matcher(pageData);
		if (match.find()) {
			Map<String, Object> dataMap;
			try {
				List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
				List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
				dataMap = (Map<String, Object>) JsonUtil.parseObject(match.group(1));
				if (dataMap.containsKey("mods")) {
					Map<String, Object> modsMap = (Map<String, Object>) dataMap.get("mods");
					if (modsMap.containsKey("listItems")) {
						List<Map<String, Object>> listItems = (List<Map<String, Object>>) modsMap.get("listItems");
						if (listItems != null && !listItems.isEmpty()) {
							for (Map<String, Object> productMap : listItems) {
								Map<String, Object> tempitemMap = new HashMap<String, Object>();
								Map<String, Object> itemlink = new HashMap<String, Object>();
								// 商品链接
								if (productMap.containsKey("productUrl")) {
									getProductUrl(productMap, tempitemMap, itemlink);
								}

								// 商品名称
								if (productMap.containsKey("name")) {
									getProductName(productMap, tempitemMap);
								}

								// 价格
								if (productMap.containsKey("priceShow")) {
									getProductPrice(productMap, tempitemMap);
								}
								items.add(tempitemMap);
							}
							resultData.put(Constants.ITEMS, items);
						}
						/**
						 * 处理下一页翻页
						 */
					}
				}
				if (dataMap.containsKey("mainInfo")) {
					Map<String, Object> mainInfoMap = (Map<String, Object>) dataMap.get("mainInfo");
					if (mainInfoMap.containsKey("pageSize") && mainInfoMap.containsKey("page")) {
						getNextPage(resultData, url, tasks, mainInfoMap);
					}
				}
				// ParseUtils.getIid(unit, result);
			} catch (Exception e) {
				LOG.error("Elazada_mys:" + " " + unit.getUrl() + "html parse failed");
			}
		}

		return new ReProcessResult(SUCCESS, processdata);
	}

	/**
	 * @param items
	 * @param productMap
	 * @param tempitemMap
	 */
	private void getProductPrice(Map<String, Object> productMap, Map<String, Object> tempitemMap) {
		String itemprice = productMap.get("priceShow").toString();
		tempitemMap.put("itemprice", itemprice);
	}

	/**
	 * @param resultData
	 * @param url
	 * @param match
	 * @param tasks
	 * @param mainInfoMap
	 */
	private void getNextPage(Map<String, Object> resultData, String url, List<Map<String, Object>> tasks,
			Map<String, Object> mainInfoMap) {
		int pagetotal = Integer.parseInt(mainInfoMap.get("pageSize").toString());
		int currentpage = Integer.parseInt(mainInfoMap.get("page").toString());
		if (pagetotal > currentpage) {
			String pageRex = "page=(\\d+)";
			String nextpage = null;
			Matcher nextPageMatch = Pattern.compile(pageRex).matcher(url);
			if (nextPageMatch.find()) {
				int pageno = Integer.parseInt(nextPageMatch.group(1));
				nextpage = url.replace("page=" + pageno, "page=" + (pageno + 1));
			} else {
				nextpage = url.concat("?page=2");
			}
			Map<String, Object> nextpageTask = new HashMap<String, Object>();
			nextpageTask.put(Constants.LINK, nextpage);
			nextpageTask.put(Constants.RAWLINK, nextpage);
			nextpageTask.put(Constants.LINKTYPE, "eclist");
			resultData.put(Constants.NEXTPAGE, nextpage);
			tasks.add(nextpageTask);
			resultData.put(Constants.TASKS, tasks);
		}
	}

	/**
	 * @param productMap
	 * @param tempitemMap
	 */
	private void getProductName(Map<String, Object> productMap, Map<String, Object> tempitemMap) {
		String name = productMap.get("name").toString();
		String itemname = name;
		tempitemMap.put("itemname", itemname);
	}

	/**
	 * @param productMap
	 * @param tempitemMap
	 * @param itemlink
	 */
	private void getProductUrl(Map<String, Object> productMap, Map<String, Object> tempitemMap,
			Map<String, Object> itemlink) {
		String linkUrl = productMap.get("productUrl").toString();
		itemlink.put("link", "https:"+linkUrl);
		itemlink.put("rawlink", "https:"+linkUrl);
		itemlink.put("linktype", "eccontent");
		tempitemMap.put("itemlink", itemlink);
	}
}