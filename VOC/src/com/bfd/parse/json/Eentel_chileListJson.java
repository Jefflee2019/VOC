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
 * 
 * @describe 组装json数据及下一页
 * @author Administrator
 * @date 2019年2月22日 上午9:19:21
 * @mind-step
 * @throws Exception
 */

public class Eentel_chileListJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(Eentel_chileListJson.class);

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
			if (jsonMap.containsKey("products")) {
				List<Map<String, Object>> productsList = (List<Map<String, Object>>) jsonMap.get("products");
				if (productsList != null && !productsList.isEmpty()) {
					List<Map<String, Object>> itemsList = new ArrayList<Map<String, Object>>();
					for (Map<String, Object> productMap : productsList) {
						// 组装产品信息
						Map<String, Object> itemMap = new HashMap<String, Object>();
						Map<String, Object> linkMap = new HashMap<String, Object>();
						// itemname
						if (productMap.containsKey("title")) {
							String itemname = productMap.get("title").toString();
							// 商品名称没有品牌，不便于后游数据落表，需要补充品牌信息
							if (productMap.containsKey("basic_features")) {
								List<Map<String, Object>> basicFeaturesList = (List<Map<String, Object>>) productMap
										.get("basic_features");
								if (basicFeaturesList != null && !basicFeaturesList.isEmpty()) {
									for (Map<String, Object> featureMap : basicFeaturesList) {
										if (featureMap.get("slug").equals("marca")) {
											String brand = featureMap.get("value").toString();
											itemname = brand + " " + itemname;
											itemMap.put(Constants.ITEMNAME, itemname);
											break;
										}
									}
								}
							}
						}
						// link
						if (productMap.containsKey("slug")) {
							String slug = productMap.get("slug").toString();
							String link = new StringBuffer()
									.append("https://equipos.entel.cl/segmentos/personas/products/").append(slug)
									.append("?modalityUrl=contratacion#multi_smart").toString();
							linkMap.put("link", link);
							linkMap.put("rawlink", link);
							linkMap.put("linktype", "eccontent");
							itemMap.put("itemlink", linkMap);
						}

						// price
						if (productMap.containsKey("plan")) {
							Map<String, Object> planMap = (Map<String, Object>) productMap.get("plan");
							if (planMap.containsKey("discount_percentage")) {
								double price = Double.parseDouble(planMap.get("discount_percentage").toString()) / 1000;
								itemMap.put("itemprice", price);
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
			if (jsonMap.containsKey("meta")) {
				Map<String, Object> metaMap = (Map<String, Object>) jsonMap.get("meta");
				if (metaMap.containsKey("current_page") && metaMap.containsKey("total_pages")) {
					int currentPage = (int) metaMap.get("current_page");
					int totalPage = (int) metaMap.get("total_pages");
					if (totalPage > currentPage) {
						Matcher match = Pattern.compile("&page=(\\d+)").matcher(url);
						if (match.find()) {
							int pageno = Integer.parseInt(match.group(1));
							nextPage = url.replace("&page=" + pageno, "&page=" + (pageno + 1));
							nextpageTask(parsedata, taskList, nextPage);
						}
					}
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
