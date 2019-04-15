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
 * Efalabella_columbia 
 * 主要功能：处理下一页及修改商品页任务链接
 * 
 * @author lth
 *
 */
public class Efalabella_columbiaListRe implements ReProcessor {

	private static final Log LOG = LogFactory.getLog(Efalabella_columbiaListRe.class);

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
										String linkUrl = productMap.get("url").toString();
										itemlink.put("link", "https://www.falabella.com.pe"+linkUrl);
										itemlink.put("rawlink", "https://www.falabella.com.pe"+linkUrl);
										itemlink.put("linktype", "eccontent");
									}
									tempitemMap.put("itemlink", itemlink);
									// 商品名称
									String brand = "";
									if(productMap.containsKey("brand")) {
									brand = productMap.get("brand").toString();
									}
									String title = productMap.get("title").toString();
									String itemname = brand + " " + title;
									tempitemMap.put("itemname", itemname);

									// 价格
									if (productMap.containsKey("prices")) {
										List<Map<String, Object>> pricesList = (List<Map<String, Object>>) productMap
												.get("prices");
										for (Map<String, Object> pricedata : pricesList) {
											if (pricedata.containsKey("label")) {
												String label = pricedata.get("label").toString();
												if (label.contains("Internet")) {
													String originalPrice = "";
													if (pricedata.containsKey("formattedLowestPrice")) {
														originalPrice = pricedata.get("formattedLowestPrice")
																.toString();
													} else {
														originalPrice = pricedata.get("originalPrice").toString();
													}
													String symbol = pricedata.get("symbol").toString();
													String price = symbol + originalPrice;
													tempitemMap.put("itemprice", price);
													break;
												}
											}
										}
									}
									items.add(tempitemMap);
									tasks.add(itemlink);
								}
								resultData.put(Constants.ITEMS, items);
								resultData.put(Constants.TASKS, tasks);
							}
						}
						/**
						 * 处理下一页翻页
						 */
						int pagetotal = 0;
						int currentpage = 0;
						if (searchItemMap.containsKey("pagesTotal")) {
							pagetotal = Integer.parseInt(searchItemMap.get("pagesTotal").toString());
						}
						if (searchItemMap.containsKey("curentPage")) {
							currentpage = Integer.parseInt(searchItemMap.get("curentPage").toString());
						}
						if (pagetotal > currentpage) {
							String pageRex = "page=(\\d+)";
							String pageno = "";
							String nextpage = null;
							Matcher nextPageMatch = Pattern.compile(pageRex).matcher(url);
							if (nextPageMatch.find()) {
								pageno = match.group(1);
								nextpage = url.replace("page=" + pageno, "page=" + (Integer.parseInt(pageno) + 1));
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
				}
				ParseUtils.getIid(unit, result);
			} catch (Exception e) {
				LOG.error("Esaga_peru:" + " " + unit.getUrl() + "html parse failed");
			}
		}

		return new ReProcessResult(SUCCESS, processdata);
	}
}