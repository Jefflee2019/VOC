package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * 站点名：(京东)沃尔玛官方旗舰店 
 *
 * cid:EwartmFlagship_wlm 
 * 
 * 主要功能： 获取价格 获取评论总数 获取促销信息
 * 
 * @author lth
 *
 */
public class EwartmFlagship_wlmContentJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(EwartmFlagship_wlmContentJson.class);

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
				json = new String(data.getData(), "GBK");

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

	public void executeParse(Map<String, Object> parsedata, String json, String url, ParseUnit unit) {
		Object obj = null;
		try {
			obj = JsonUtil.parseObject(json);
		} catch (Exception e) {
			LOG.error("json parse error or json is null");
		}
		
		/**
		 * 获取促销信息(部分地区，默认深圳)
		 * //https://cd.jd.com/promotion/v2?skuId=1216716&area=19_1607_3155_0&shopId=1000002668&cat=1319,1523,7052
		 */
		if (url.contains("promotion")) {
			getProm(parsedata, obj);
		}

		/**
		 * 获取各类评论数 
		 * http://club.jd.com/ProductPageService.aspx?method=GetCommentSummaryBySkuId&referenceId=1216716
		 */
		if (url.contains("CommentSummary") && obj instanceof Map) {
			getCommentCount(parsedata, obj);
		}

		/**
		 * 获取库存(默认深圳) 
		 * https://c0.3.cn/stocks?type=getstocks&skuIds=4787565&area=19_1607_3155_0
		 */
		if (url.contains("stocks")) {
			getStockState(parsedata, url, obj);
		}
	}

	@SuppressWarnings("unchecked")
	private void getStockState(Map<String, Object> parsedata, String url, Object obj) {
		Boolean stock_state = true;
		Map<String, Object> map = (Map<String, Object>) obj;
		Matcher match = Pattern.compile("skuIds=(\\d+)").matcher(url);
		if (match.find()) {
			String itemID = match.group(1);
			if (map.containsKey(itemID)) {
				Map<String, Object> temp = (Map<String, Object>) map.get(itemID);
				if (temp.containsKey("StockState")) {
					String stock = temp.get("StockState").toString();
					if (stock.equals("33")) {
						stock_state = true;
					} else {
						stock_state = false;
					}
					parsedata.put("stockState", stock_state);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void getCommentCount(Map<String, Object> parsedata, Object obj) {
		Map<String, Object> map = (Map<String, Object>) obj;
		// 整合评论数
		Map<String, Object> comCountMap = new HashMap<String, Object>();
		// 评论总数
		if (map.containsKey("CommentCount")) {
			int replyCnt = Integer.parseInt(map.get("CommentCount").toString());
			comCountMap.put(Constants.REPLY_CNT, replyCnt);
		}

		// 好评数
		if (map.containsKey("GoodCount")) {
			int goodCnt = Integer.parseInt(map.get("GoodCount").toString());
			comCountMap.put(Constants.GOOD_CNT, goodCnt);
		}

		// 中评数
		if (map.containsKey("GeneralCount")) {
			int generalCnt = Integer.parseInt(map.get("GeneralCount").toString());
			comCountMap.put(Constants.GENERAL_CNT, generalCnt);
		}

		// 差评数
		if (map.containsKey("PoorCount")) {
			int poorCnt = Integer.parseInt(map.get("PoorCount").toString());
			comCountMap.put(Constants.POOR_CNT, poorCnt);
		}

		// 追评数
		if (map.containsKey("AfterCount")) {
			int afterCount = Integer.parseInt(map.get("AfterCount").toString());
			comCountMap.put(Constants.AGAIN_CNT, afterCount);
		}

		// 好评率
		if (map.containsKey("GoodRateShow")) {
			int goodRate = Integer.parseInt(map.get("GoodRateShow").toString());
			comCountMap.put(Constants.GOOD_RATE, goodRate);
		}
		parsedata.put("commentCount", comCountMap);
	}

	@SuppressWarnings("unchecked")
	private void getProm(Map<String, Object> parsedata, Object obj) {
		if (obj instanceof Map) {
			Map<String, Object> map = (Map<String, Object>) obj;
			// 整合促销信息
			Map<String, Object> promInfo = new HashMap<String, Object>();
			// 返券活动
			if (map.containsKey("quan")) {
				// 有值的时候为map，无则为list
				Object quan = map.get("quan");
				if (quan instanceof Map) {
					Map<String, Object> quanMap = (Map<String, Object>) quan;
					String prom_quan = quanMap.get("title").toString();
					promInfo.put("prom_quan", prom_quan);
				}
			}

			if (map.containsKey("prom")) {
				Object prom = map.get("prom");
				if (prom instanceof Map) {
					Map<String, Object> promMap = (Map<String, Object>) prom;
					// 加价购
					List<Map<String, Object>> pickOneTag = (List<Map<String, Object>>) promMap.get("pickOneTag");
					List<String> plusBuy = new ArrayList<String>();
					if (pickOneTag != null && !pickOneTag.isEmpty()) {
						for (int i = 0; i < pickOneTag.size(); i++) {
							plusBuy.add(pickOneTag.get(i).get("content").toString());
						}
						promInfo.put("plusBuy", plusBuy);
					}
				}
			}
			parsedata.put("promInfo", promInfo);
		}
	}
}
