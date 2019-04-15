package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * 站点名：一号店 主要功能： 获取价格、评论数、促销信息、店铺名称
 * 
 * @author lth
 *
 */
public class Eyhd_wlmContentJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(Eyhd_wlmContentJson.class);

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
		} catch (Exception e) {
			LOG.error("json parse error or json is null");
		}

		/**
		 * @function 处理价格
		 */

		if (url.contains("getPrices")) {
			// (上海)获取价格
			// http://item.yhd.com/api/item/getPrices.do?params.area=2_2823_51974_0&params.skuIds=4801907
			if (url.contains("2_2823_51974_0")) {
				String flag = "sh";
				getPrice(parsedata, obj, flag);
			}

			// (北京)获取价格
			// http://item.yhd.com/api/item/getPrices.do?params.area=1_2802_2821_0&params.skuIds=4801907
			if (url.contains("1_2802_2821_0")) {
				String flag = "bj";
				getPrice(parsedata, obj, flag);
			}

			// (成都)获取价格
			// http://item.yhd.com/api/item/getPrices.do?params.area=22_1930_4773_0&params.skuIds=4801907
			if (url.contains("22_1930_4773_0")) {
				String flag = "cd";
				getPrice(parsedata, obj, flag);
			}

			// (深圳)获取价格
			// http://item.yhd.com/api/item/getPrices.do?params.area=19_1607_3155_0&params.skuIds=4801907
			if (url.contains("19_1607_3155_0")) {
				String flag = "sz";
				getPrice(parsedata, obj, flag);
			}
			
			//针对某个地区在某一时段会请求不到价格的情况的处理
			if (!parsedata.containsKey("sz_price")) {
				String price = "";
				if (parsedata.containsKey("sh_price")) {
					price = parsedata.get("sh_price").toString();
					if (!price.equals("0")) {
						parsedata.put("sz_price", price);
					}
				} else if (parsedata.containsKey("bj_price")) {
					price = parsedata.get("bj_price").toString();
					if (!price.equals("0")) {
						parsedata.put("sz_price", price);
					}
				} else if (parsedata.containsKey("cd_price")) {
					price = parsedata.get("cd_price").toString();
					if (!price.equals("0")) {
						parsedata.put("sz_price", price);
					}
				}
			}

			if (!parsedata.containsKey("sh_price")) {
				String price = "";
				if (parsedata.containsKey("sz_price")) {
					price = parsedata.get("sz_price").toString();
					if (!price.equals("0")) {
						parsedata.put("sh_price", price);
					}
				} else if (parsedata.containsKey("bj_price")) {
					price = parsedata.get("bj_price").toString();
					if (!price.equals("0")) {
						parsedata.put("sh_price", price);
					}
				} else if (parsedata.containsKey("cd_price")) {
					price = parsedata.get("cd_price").toString();
					if (!price.equals("0")) {
						parsedata.put("sh_price", price);
					}
				}
			}

			if (!parsedata.containsKey("bj_price")) {
				String price = "";
				if (parsedata.containsKey("sh_price")) {
					price = parsedata.get("sh_price").toString();
					if (!price.equals("0")) {
						parsedata.put("bj_price", price);
					}
				} else if (parsedata.containsKey("sz_price")) {
					price = parsedata.get("sz_price").toString();
					if (!price.equals("0")) {
						parsedata.put("bj_price", price);
					}
				} else if (parsedata.containsKey("cd_price")) {
					price = parsedata.get("cd_price").toString();
					if (!price.equals("0")) {
						parsedata.put("bj_price", price);
					}
				}
			}

			if (!parsedata.containsKey("cd_price")) {
				String price = "";
				if (parsedata.containsKey("sh_price")) {
					price = parsedata.get("sh_price").toString();
					if (!price.equals("0")) {
						parsedata.put("cd_price", price);
					}
				} else if (parsedata.containsKey("bj_price")) {
					price = parsedata.get("bj_price").toString();
					if (!price.equals("0")) {
						parsedata.put("cd_price", price);
					}
				} else if (parsedata.containsKey("sz_price")) {
					price = parsedata.get("sz_price").toString();
					if (!price.equals("0")) {
						parsedata.put("cd_price", price);
					}
				}
			}
		}
		
		
		
		
		/**
		 * 获取各类评论数
		 * eg:http://e.yhd.com/squ/comment/getFuzzyProductCommentSummarys.do?productId=4801907
		 */

		if (url.contains("getFuzzyProductCommentSummarys") && obj instanceof Map) {
			Map<String, Object> map = (Map<String, Object>) obj;
			// 与其他格式保持结构一致
			Map<String, Object> commentCount = new HashMap<String, Object>();
			if (map.containsKey("data")) {
				Map<String, Object> dataMap = (Map<String, Object>) map.get("data");
				if (dataMap.containsKey("result")) {
					Map<String, Object> resultMap = (Map<String, Object>) dataMap.get("result");
					if (resultMap.containsKey("commentSummaries")) {
						List<Map<String, Object>> commentSummaries = (List<Map<String, Object>>) resultMap
								.get("commentSummaries");
						if (commentSummaries != null && !commentSummaries.isEmpty()) {
							Map<String, Object> commMap = commentSummaries.get(0);
							// 总评论数
							if (commMap.containsKey("CommentCount")) {
								int replyCnt = Integer.parseInt(commMap.get("CommentCount").toString());
								commentCount.put(Constants.REPLY_CNT, replyCnt);
							}

							// 好评数
							if (commMap.containsKey("GoodCount")) {
								int goodCnt = Integer.parseInt(commMap.get("GoodCount").toString());
								commentCount.put(Constants.GOOD_CNT, goodCnt);
							}

							// 中评数
							if (commMap.containsKey("GeneralCount")) {
								int generalCnt = Integer.parseInt(commMap.get("GeneralCount").toString());
								commentCount.put(Constants.GENERAL_CNT, generalCnt);
							}

							// 差评数
							if (commMap.containsKey("PoorCount")) {
								int poorCnt = Integer.parseInt(commMap.get("PoorCount").toString());
								commentCount.put(Constants.POOR_CNT, poorCnt);
							}

							// 追评数
							if (commMap.containsKey("AfterCount")) {
								int afterCount = Integer.parseInt(commMap.get("AfterCount").toString());
								commentCount.put(Constants.AGAIN_CNT, afterCount);
							}

							// 好评率
							if (commMap.containsKey("GoodRateShow")) {
								int goodRate = Integer.parseInt(commMap.get("GoodRateShow").toString());
								commentCount.put(Constants.GOOD_RATE, goodRate);
							}
						}
					}
				}
				parsedata.put("commentCount", commentCount);
			}
		}

		/**
		 * @function 分4个地区(北京、上海、成都、深圳)提取库存
		 */
		if (url.contains("stock")) {
			// 上海地区 库存
			// http://c0.3.cn/stock?extraParam=%7B%22originid%22:%221%22%7D&ch=1&skuId=4801907&area=2_2823_51974_0&cat=12218%2C12221%2C13556&venderId=1000077502
			if (url.contains("2_2823_51974_0")) {
				String flag = "sh";
				toGetStockState(parsedata, obj, flag);
			}

			// 北京地区 库存
			// http://c0.3.cn/stock?extraParam=%7B%22originid%22:%221%22%7D&ch=1&skuId=4801907&area=1_2802_2821_0&cat=12218%2C12221%2C13556&venderId=1000077502
			if (url.contains("1_2802_2821_0")) {
				String flag = "bj";
				toGetStockState(parsedata, obj, flag);
			}

			// 成都地区 库存
			// http://c0.3.cn/stock?extraParam=%7B%22originid%22:%221%22%7D&ch=1&skuId=4801907&area=22_1930_4773_0&cat=12218%2C12221%2C13556&venderId=1000077502
			if (url.contains("22_1930_4773_0")) {
				String flag = "cd";
				toGetStockState(parsedata, obj, flag);
			}

			// 深圳地区 库存
			// http://c0.3.cn/stock?extraParam=%7B%22originid%22:%221%22%7D&ch=1&skuId=4801907&area=19_1607_3155_0&cat=12218%2C12221%2C13556&venderId=1000077502
			if (url.contains("19_1607_3155_0")) {
				String flag = "sz";
				toGetStockState(parsedata, obj, flag);
			}
		}
		
		/**
		 * @function 获取促销信息
		 * @note 促销信息反爬严重，不分地区分别请求
		 */
		// http://item.yhd.com/api/item/ajaxGetPromoInfo.do?params.skuId=4801907&params.venderId=1000077502&params.area=1_2802_2821_0
		if (url.contains("ajaxGetPromoInfo") && obj instanceof Map) {
			Map<String, Object> promotionData = (Map<String, Object>) obj;
			List<String> plusBuy = new ArrayList<String>();
			Map<String, Object> promotionMap = new HashMap<String, Object>();
			if (promotionData.containsKey("data")) {
				Map<String,Object> dataMap = (Map<String, Object>) promotionData.get("data");
				if (dataMap.containsKey("promos")) {
					Map<String,Object> promos = (Map<String, Object>) dataMap.get("promos");
					// 1-限购  2-满赠  3-满减  4-跨店满减 5-满折 (1、2不需要)
					String promoCode = "";
					if(promos.containsKey("3")) {
						promoCode = "3";
						getPromotion(plusBuy, promos,promoCode);
					}
					
					if(promos.containsKey("4")) {
						promoCode = "4";
						getPromotion(plusBuy, promos,promoCode);
					}
					
					if(promos.containsKey("5")) {
						promoCode = "5";
						getPromotion(plusBuy, promos,promoCode);
					}
					
					promotionMap.put("plusBuy", plusBuy);
					parsedata.put("sh_promInfo", promotionMap);
					parsedata.put("bj_promInfo", promotionMap);
					parsedata.put("cd_promInfo", promotionMap);
					parsedata.put("sz_promInfo", promotionMap);
				}
			}
		}
		
		/**
		 * @function 获取店铺
		 * @note 改版后，店铺为动态请求
		 */
		if (url.contains("ajaxGetVenderInfo")) {
			String storeData = (String) json;
			Document doc = Jsoup.parse(storeData);
			Elements ele = doc.select("Strong>a");
			String text = ele.text();
			parsedata.put(Constants.STORENAME, text);
		}
	}

	/**
	 * @param plusBuy
	 * @param promos
	 */
	@SuppressWarnings("unchecked")
	private void getPromotion(List<String> plusBuy, Map<String, Object> promos,String promoCode) {
		List<Map<String, Object>> fullCut = (List<Map<String, Object>>) promos.get(promoCode);
		if (fullCut != null && !fullCut.isEmpty()) {
			for (Map<String, Object> promo : fullCut) {
				if (promo.containsKey("content")) {
					String content = promo.get("content").toString();
					plusBuy.add(content);
				}
			}
		}
	}
	
	/**
	 * @param parsedata
	 * @param obj
	 * @param flag
	 */

	@SuppressWarnings("unchecked")
	private void toGetStockState(Map<String, Object> parsedata, Object obj, String flag) {
		if (obj instanceof Map) {
			Map<String, Object> map = (Map<String, Object>) obj;
			if (map.containsKey("stock")) {
				Map<String, Object> stockMap = (Map<String, Object>) map.get("stock");
				if (stockMap.containsKey("StockState")) {
					String stockState = stockMap.get("StockState").toString();
					Boolean stockStatus = true;
					// stockState 33-现货
					if (stockState.equals("33")) {
						stockStatus = true;
					} else {
						stockStatus = false;
					}
					parsedata.put(flag + "_stockState", stockStatus);
				}
			}

		}
	}

	/**
	 * @param parsedata
	 * @param obj
	 * @param flag
	 */
	@SuppressWarnings("unchecked")
	private void getPrice(Map<String, Object> parsedata, Object obj, String flag) {
		if (obj instanceof Map) {
			Map<String, Object> priceData = (Map<String, Object>) obj;
			if (priceData.containsKey("data")) {
				List<Map<String, Object>> data = (List<Map<String, Object>>) priceData.get("data");
				if (data != null && !data.isEmpty()) {
					Map<String, Object> priceMap = data.get(0);
					if (priceMap.containsKey("p")) {
						String price = priceMap.get("p").toString();
						parsedata.put(flag + "_price", Double.parseDouble(price));
					}
				}
			}
		}
	}
}
