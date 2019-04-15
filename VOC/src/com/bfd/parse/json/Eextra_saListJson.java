package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.TextUtil;

/**
 * Desc:Eextra_sa Json插件组装商品信息
 * 
 * @describe
 * @author Administrator
 * @date 2019年3月13日 下午5:04:45
 * @mind-step
 * @throws Exception
 */

public class Eextra_saListJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(Eextra_saListJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient normalizerClient, ParseUnit unit) {
		int parseCode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
		for (JsonData data : dataList) {
			if (!data.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(data, unit);
			try {
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0 && (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["), json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
				}
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
				LOG.warn("json :" + json + ".url:" + taskdata.get("url"));
				LOG.info("goodsList exception during executeParse");
				LOG.warn("AMJsonParser exception, taskdata url=" + taskdata.get("url") + ".jsonUrl :" + data.getUrl(),
						e);
			}
		}
		JsonParserResult result = new JsonParserResult();
		try {
			result.setData(parsedata);
			result.setParsecode(parseCode);
		} catch (Exception e) {
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

	/**
	 * execute parse
	 * 
	 * @param parsedata
	 * @param json
	 * @param url
	 * @param unit
	 */
	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata, String json, String url, ParseUnit unit) {
		try {
			Map<String, Object> jsonMap = (Map<String, Object>) JsonUtils.parseObject(json);
			if (jsonMap.containsKey("Products")) {
				List<Map<String, Object>> productsList = (List<Map<String, Object>>) jsonMap.get("Products");
				if (productsList != null && !productsList.isEmpty()) {
					List<Map<String, Object>> itemsList = new ArrayList<Map<String, Object>>();
					for (Map<String, Object> productMap : productsList) {
						// 组装产品信息
						Map<String, Object> itemMap = new HashMap<String, Object>();
						Map<String, Object> linkMap = new HashMap<String, Object>();
						// itemname
						if (productMap.containsKey("GtmUrlName")) {
							String itemname = productMap.get("GtmUrlName").toString();
							itemMap.put(Constants.ITEMNAME, itemname);
						}
						// link
						if (productMap.containsKey("GtmProductFriendlyUrl")) {
							String gtmProductFriendlyUrl = productMap.get("GtmProductFriendlyUrl").toString();
							String link = new StringBuffer()
									.append("http://www.extrastores.com")
									.append(gtmProductFriendlyUrl).toString();
							linkMap.put("link", link);
							linkMap.put("rawlink", link);
							linkMap.put("linktype", "eccontent");
							itemMap.put("itemlink", linkMap);
						}

						// price
						if (productMap.containsKey("ProductAcitvePrice")) {
							double price = (double) productMap.get("ProductAcitvePrice");
							itemMap.put("itemprice", price);
						}
						
						// stock 是否缺货标记,如果soldOut=1，则stock记为"Y"，否则忽略该字段
						if(productMap.containsKey("SoldOut")) {
							int soldOut = (int) productMap.get("SoldOut");
							if(soldOut == 1) {
								itemMap.put("stock","Y");
							}
						}
						itemsList.add(itemMap);
					}
					parsedata.put(Constants.ITEMS, itemsList);
				}
			}

			List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
			parsedata.put("tasks", taskList);
			// 处理下一页
			String nextPage = null;
			if (jsonMap.containsKey("MaxPage") && jsonMap.containsKey("CurrentPage")) {
				int currentPage = (int) jsonMap.get("CurrentPage");
				int totalPage = (int) jsonMap.get("MaxPage");
				if (totalPage > currentPage) {
					Matcher match = Pattern.compile("&page=(\\d+)").matcher(url);
					if (match.find()) {
						int pageno = Integer.parseInt(match.group(1));
						nextPage = url.replace("&page=" + pageno, "&page=" + (pageno + 1));
					} else {
						nextPage = url.concat("&page=2");
					}
					nextpageTask(parsedata, taskList, nextPage);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("executeParse error " + url);
			LOG.info("goodstList error!");
		}
	}

	/**
	 * @param parsedata
	 * @param taskList
	 */
	private void nextpageTask(Map<String, Object> parsedata, List<Map<String, Object>> taskList, String nextpage) {
		Map<String, Object> nextpageTask = new HashMap<String, Object>();
		nextpageTask.put("link", nextpage);
		nextpageTask.put("rawlink", nextpage);
		nextpageTask.put("linktype", "eclist");
		taskList.add(nextpageTask);
		parsedata.put("nextpage", nextpageTask);
		parsedata.put("tasks", taskList);
	}
}
