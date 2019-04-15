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
import com.bfd.parse.util.ParseUtils;

/**
 * Efalabella_columbia 主要功能：处理下一页及修改商品页任务链接
 * 
 * @author lth
 *
 */
public class Efalabella_chileListRe implements ReProcessor {

	private static final Log LOG = LogFactory.getLog(Efalabella_chileListRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		String url = unit.getUrl();

		String pageData = unit.getPageData();
		Matcher match = Pattern.compile(
				"var\\s*fbra_browseProductListConfig\\s*=\\s*(.*);\\s*var\\s*fbra_browseProductList").matcher(pageData);
		if (match.find()) {
			Map<String, Object> priceMap;
			try {
				List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
				List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
				priceMap = (Map<String, Object>) JsonUtil.parseObject(match.group(1));
				if (priceMap.containsKey("state")) {
					Map<String, Object> stateMap = (Map<String, Object>) priceMap.get("state");
					if (stateMap.containsKey("searchItemList")) {
						Map<String, Object> searchItemMap = (Map<String, Object>) stateMap.get("searchItemList");
						if (searchItemMap.containsKey("resultList")) {
							List<Map<String, Object>> resultList = (List<Map<String, Object>>) searchItemMap
									.get("resultList");
							if (resultList != null && !resultList.isEmpty()) {
								for (Map<String, Object> productMap : resultList) {
									Map<String, Object> tempitemMap = new HashMap<String, Object>();
									Map<String, Object> itemlink = new HashMap<String, Object>();
									// 商品链接
									if (productMap.containsKey("url")) {
										getProductUrl(productMap, tempitemMap, itemlink);
									}

									// 商品名称
									if (productMap.containsKey("brand") && productMap.containsKey("title")) {
										getProductName(productMap, tempitemMap);
									}
									// 价格
									if (productMap.containsKey("prices")) {
										getProductPrice(items, productMap, tempitemMap);
									}
								}
								resultData.put(Constants.ITEMS, items);
							}
						}
						/**
						 * 处理下一页翻页
						 */
						if (searchItemMap.containsKey("pagesTotal") && searchItemMap.containsKey("curentPage")) {
							getNextPage(resultData, url, tasks, searchItemMap);
						}
					}
				}
				ParseUtils.getIid(unit, result);
			} catch (Exception e) {
				LOG.error("Esaga_peru:" + " " + unit.getUrl() + "html parse failed");
			}
		}

		return new ReProcessResult(SUCCESS, processdata);
	}

	/**
	 * @param items
	 * @param productMap
	 * @param tempitemMap
	 */
	@SuppressWarnings("unchecked")
	private void getProductPrice(List<Map<String, Object>> items, Map<String, Object> productMap,
			Map<String, Object> tempitemMap) {
		List<Map<String, Object>> pricesList = (List<Map<String, Object>>) productMap.get("prices");
		for (Map<String, Object> pricedata : pricesList) {
			// 会存在多个价格及价格区间。原则是取低价，区间的情况取最低
			String price = "";
			if (pricesList.size() > 1) {// 存在多个价格时
				// type=1是低价，type=3是高价
				if (pricedata.get("type").equals(1)) {
					price = getPrice(pricedata);
					tempitemMap.put("itemprice", price);
					items.add(tempitemMap);
					break;
					//排除type=2 特殊价格
				}else if(pricedata.get("type").equals(2)) {
					continue;
				}else {
					price = getPrice(pricedata);
				}
			} else {
				price = getPrice(pricedata);
			}
			tempitemMap.put("itemprice", price);
			items.add(tempitemMap);
		}
	}

	/**
	 * @param pricedata
	 * @return
	 */
	private String getPrice(Map<String, Object> pricedata) {
		String price;
		if (pricedata.containsKey("formattedLowestPrice")) {
			price = pricedata.get("formattedLowestPrice").toString();
		} else {
			price = pricedata.get("originalPrice").toString();
		}
		return price;
	}

	/**
	 * @param resultData
	 * @param url
	 * @param match
	 * @param tasks
	 * @param searchItemMap
	 */
	private void getNextPage(Map<String, Object> resultData, String url,
			List<Map<String, Object>> tasks, Map<String, Object> searchItemMap) {
		int pagetotal = Integer.parseInt(searchItemMap.get("pagesTotal").toString());
		int currentpage = Integer.parseInt(searchItemMap.get("curentPage").toString());
		if (pagetotal > currentpage) {
			String pageRex = "page=(\\d+)";
			String nextpage = null;
			Matcher nextPageMatch = Pattern.compile(pageRex).matcher(url);
			if (nextPageMatch.find()) {
				int pageno = Integer.parseInt(nextPageMatch.group(1));
				nextpage = url.replace("page=" + pageno, "page=" + (pageno+1));
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
		String brand = productMap.get("brand").toString();
		String title = productMap.get("title").toString();
		String itemname = brand + " " + title;
		tempitemMap.put("itemname", itemname);
	}

	/**
	 * @param productMap
	 * @param tempitemMap
	 * @param itemlink
	 */
	private void getProductUrl(Map<String, Object> productMap, Map<String, Object> tempitemMap,
			Map<String, Object> itemlink) {
		String linkUrl = productMap.get("url").toString();
		itemlink.put("link", "https://www.falabella.com.pe" + linkUrl);
		itemlink.put("rawlink", "https://www.falabella.com.pe" + linkUrl);
		itemlink.put("linktype", "eccontent");
		tempitemMap.put("itemlink", itemlink);
	}
}