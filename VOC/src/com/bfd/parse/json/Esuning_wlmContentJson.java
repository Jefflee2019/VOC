package com.bfd.parse.json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * 站点名：苏宁易购
 * 
 * 主要功能： 获取价格 获取评论总数 获取促销信息
 * 
 * @author lth
 *
 */
public class Esuning_wlmContentJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(Esuning_wlmContentJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient urlnormalizerClients, ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();

		// 遍历dataList
		for (Object obj : dataList) {
			JsonData data = (JsonData) obj;
			// 判断该ajax数据是否下载成功
			if (!data.downloadSuccess()) {
				continue;
			}
			// 解压缩ajax数据
			String json = TextUtil.getUnzipJson(data, unit);

			try {
				json = new String(data.getData(), "utf8");

				// 将ajax数据转化为json数据格式
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0 && (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["), json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
				}
				// 执行从json数据中提取自己感兴趣的数据
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
				// e.printStackTrace();
				// LOG.warn("json:" + json + ".url:" + taskdata.get("url"));
				LOG.warn("AMJsonParse exception,taskdat url=" + taskdata.get("url") + ".jsonUrl:" + data.getUrl(), e);
			}
		}

		// 组装返回结果
		JsonParserResult result = new JsonParserResult();
		try {
			result.setParsecode(parsecode);
			result.setData(parsedata);
		} catch (Exception e) {
			// e.printStackTrace();
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata, String json, String url, ParseUnit unit) {
		Object obj = null;
		try {
			obj = JsonUtil.parseObject(json);
			// obj = com.alibaba.fastjson.JSON.parse(json);
		} catch (Exception e) {
			LOG.error("json parse error or json is null");
		}
		// (上海)获取价格、库存
		// https://pas.suning.com/nspcsale_0_000000000127700348_000000000127700348_0000000000_20_021_0211301_313004_1000267_9264_12125_Z001___R9000371.html
		/*
		 * if (url.contains("nspcsale") && url.contains("021_0211301")) { String
		 * flag = "sh"; getPriceNstockState(parsedata, obj, flag); } //
		 * (北京)获取价格、库存 if (url.contains("nspcsale") &&
		 * url.contains("010_0100101")) { String flag = "bj";
		 * getPriceNstockState(parsedata, obj, flag); } // (成都)获取价格、库存 if
		 * (url.contains("nspcsale") && url.contains("028_0280101")) { String
		 * flag = "cd"; getPriceNstockState(parsedata, obj, flag); }
		 */
		// (深圳)获取价格、库存
		if (url.contains("nspcsale") && url.contains("755_7550101")) {
			// String flag = "sz";
			// getPriceNstockState(parsedata, obj, flag);
			getPriceNstockState(parsedata, obj);
		}

		/**
		 * 获取各类评论数 https://review.suning.com/ajax/review_satisfy/general-
		 * 000000000134903460-0070114511-----satisfy.htm
		 */

		if (url.contains("review_satisfy") && obj instanceof Map) {
			Map<String, Object> map = (Map<String, Object>) obj;
			if (map.containsKey("reviewCounts")) {
				getCommentcount(parsedata, map);
			}
		}

		/**
		 * 促销信息 分4个地区获取 ---------- ---------**促销信息在后处理插件获取**-------- 09/19
		 * 
		 */
	}

	@SuppressWarnings("unchecked")
	private void getCommentcount(Map<String, Object> parsedata, Map<String, Object> map) {
		Map<String, Object> commentCount = new HashMap<String, Object>();

		Object obj = map.get("reviewCounts");
		if (obj instanceof List) {
			List<Map<String, Object>> commList = (List<Map<String, Object>>) obj;
			if (commList != null && !commList.isEmpty()) {
				Map<String, Object> reviewCountsMap = ((List<Map<String, Object>>) map.get("reviewCounts")).get(0);
				String fiveStarCount = (reviewCountsMap.containsKey("fiveStarCount") ? reviewCountsMap.get(
						"fiveStarCount").toString() : "0");
				String fourStarCount = (reviewCountsMap.containsKey("fourStarCount") ? reviewCountsMap.get(
						"fourStarCount").toString() : "0");
				String threeStarCount = (reviewCountsMap.containsKey("threeStarCount") ? reviewCountsMap.get(
						"threeStarCount").toString() : "0");
				String twoStarCount = (reviewCountsMap.containsKey("twoStarCount") ? reviewCountsMap
						.get("twoStarCount").toString() : "0");
				String oneStarCount = (reviewCountsMap.containsKey("oneStarCount") ? reviewCountsMap
						.get("oneStarCount").toString() : "0");
				String replyCnt = (reviewCountsMap.containsKey("totalCount") ? reviewCountsMap.get("totalCount")
						.toString() : "0");
				String againCount = (reviewCountsMap.containsKey("againCount") ? reviewCountsMap.get("againCount")
						.toString() : "0");

				int goodcnt = Integer.parseInt(fiveStarCount) + Integer.parseInt(fourStarCount);
				int generalcnt = Integer.parseInt(threeStarCount) + Integer.parseInt(twoStarCount);
				int badcnt = Integer.parseInt(oneStarCount);
				commentCount.put(Constants.GOOD_CNT, goodcnt);
				commentCount.put(Constants.GENERAL_CNT, generalcnt);
				commentCount.put(Constants.POOR_CNT, badcnt);
				commentCount.put(Constants.REPLY_CNT, replyCnt);
				commentCount.put(Constants.AGAIN_CNT, againCount);
				parsedata.put("commentCount", commentCount);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void getPriceNstockState(Map<String, Object> parsedata, Object obj) {
		Map<String, Object> map = (Map<String, Object>) obj;
		if (map.containsKey("data")) {
			Map<String, Object> dataMap = (Map<String, Object>) map.get("data");
			// 价格
			if (dataMap.containsKey("price")) {
				getPrice(parsedata, dataMap);
			}
			// 库存
			Boolean stockState = true;
			if (dataMap.containsKey("deliverableFlag")) {
				String deliverableFlag = dataMap.get("deliverableFlag").toString();
				stockState = deliverableFlag.equals("Y") ? true : false;
			} else if (dataMap.containsKey("prescription")) {
				Map<String, Object> prescriptionMap = (Map<String, Object>) dataMap.get("prescription");
				if (prescriptionMap.containsKey("inventoryText")) {
					String inventoryText = prescriptionMap.get("inventoryText").toString();
					stockState = inventoryText.contains("现货") ? true : false;
				}
			} else {
				stockState = false;
				LOG.info("stockState of the product is 无货 or 不在地区销售");
			}
			parsedata.put("sz_stockState", stockState);
			parsedata.put("sh_stockState", stockState);
			parsedata.put("bj_stockState", stockState);
			parsedata.put("cd_stockState", stockState);
		}
	}

	@SuppressWarnings("unchecked")
	private void getPrice(Map<String, Object> parsedata, Map<String, Object> dataMap) {
		Map<String, Object> priceMap = (Map<String, Object>) dataMap.get("price");
		if (priceMap.containsKey("saleInfo") && priceMap.get("saleInfo") instanceof List) {
			List<Map<String, Object>> saleInfoList = (List<Map<String, Object>>) priceMap.get("saleInfo");
			if (saleInfoList != null && !saleInfoList.isEmpty()) {
				for (Map<String, Object> saleInfo : saleInfoList) {
					if (saleInfo.containsKey("promotionPrice") && !(saleInfo.get("promotionPrice")).equals("")) {
						String promotionPrice = saleInfo.get("promotionPrice").toString();
						if (promotionPrice.contains("-")) {
							promotionPrice = promotionPrice.split("-")[0];
						}
						Double price = Double.parseDouble(promotionPrice);
						parsedata.put("sz_price", price);
						parsedata.put("sh_price", price);
						parsedata.put("bj_price", price);
						parsedata.put("cd_price", price);
						break;// 默认第一个
					} else {
						// 不在地区销售，没有价格
						// parsedata.put(flag + "_price", "");
						parsedata.put("sz_price", "");
						parsedata.put("sh_price", "");
						parsedata.put("bj_price", "");
						parsedata.put("cd_price", "");
						LOG.info("不在地区销售");
					}
				}
			}
		}
	}
}