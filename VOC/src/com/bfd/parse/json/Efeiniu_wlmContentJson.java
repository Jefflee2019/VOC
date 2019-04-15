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
 * 站点名： 飞牛网 主要功能： 获取价格、评论数、促销信息、库存
 * 
 * @author lth
 *
 */
public class Efeiniu_wlmContentJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(Efeiniu_wlmContentJson.class);

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
		 * 获取各类评论数
		 * eg:http://item.feiniu.com/getProductCommentVo?goodsId=100945801
		 * &v=1507885706000
		 */

		if (url.contains("getProductCommentVo") && obj instanceof Map) {
			Map<String, Object> map = (Map<String, Object>) obj;
			// 与其他格式保持结构一致
			Map<String, Object> commentCount = new HashMap<String, Object>();
			if (map.containsKey("data")) {
				Map<String, Object> dataMap = (Map<String, Object>) map.get("data");
				// 总评论数
				if (dataMap.containsKey("commentUserNum")) {
					int replyCnt = Integer.parseInt(dataMap.get("commentUserNum").toString());
					commentCount.put(Constants.REPLY_CNT, replyCnt);
				}
				// 好评数
				if (dataMap.containsKey("goodCommentCount")) {
					int goodCnt = Integer.parseInt(dataMap.get("goodCommentCount").toString());
					commentCount.put(Constants.GOOD_CNT, goodCnt);
				}

				// 中评数
				if (dataMap.containsKey("normalCommentCount")) {
					int generalCnt = Integer.parseInt(dataMap.get("normalCommentCount").toString());
					commentCount.put(Constants.GENERAL_CNT, generalCnt);
				}

				// 差评数
				if (dataMap.containsKey("badCommentCount")) {
					int poorCnt = Integer.parseInt(dataMap.get("badCommentCount").toString());
					commentCount.put(Constants.POOR_CNT, poorCnt);
				}

				// 追评数
				if (dataMap.containsKey("addCommentCount")) {
					int afterCount = Integer.parseInt(dataMap.get("addCommentCount").toString());
					commentCount.put(Constants.AGAIN_CNT, afterCount);
				}

				// 好评率
				if (dataMap.containsKey("goodCommentRate")) {
					int goodRate = Integer.parseInt(dataMap.get("goodCommentRate").toString());
					commentCount.put(Constants.GOOD_RATE, goodRate);
				}
				parsedata.put("commentCount", commentCount);
			}
		}

		/**
		 * @function price 价格 和 stockStatus 库存
		 * @note 飞牛自营和非自营的请求url格式不一样
		 * 自营：http://item.feiniu.com/price_qty_sku?sku_seqs=KS1170590300439846
		 * 非自营：http://item.feiniu.com/getProductPriceStock?skuId=90301847010&v=1507865209000
		 */

		// 自营
		if (url.contains("price")||url.contains("Price")) {
			Double price = 0.0;
			Boolean stockState = true;
			if (url.contains("price_qty_sku")) {
				// 提取价格，以便提取当前商品的价格或库存
				String priceRegex = "sku_seqs=(\\S*)";
				String itemCode = getItemCodeReg(url, priceRegex);

				Map<String, Object> priceData = (Map<String, Object>) obj;
				if (priceData.containsKey("data")) {
					Map<String, Object> data = (Map<String, Object>) priceData.get("data");
					if (data.containsKey("skuSeq_list")) {
						Map<String, Object> skuSeq = (Map<String, Object>) data.get("skuSeq_list");
						if (skuSeq.containsKey(itemCode)) {
							Map<String, Object> priceMap = (Map<String, Object>) skuSeq.get(itemCode);
							if (priceMap.containsKey("sale_price")) {
								String salePrice = priceMap.get("sale_price").toString();
								// 从价格是否为空，判断是否有库存。价格为空-库存无，反之...
								if (salePrice.equals("")) {
									price = 0.0;
									stockState = false;
								} else {
									price = Double.parseDouble(salePrice);
									stockState = true;
								}
							}
						}
					}
				}
			}

			// 非自营
			if (url.contains("getProductPriceStock")) {
				Map<String, Object> priceData = (Map<String, Object>) obj;
				String priceRegex = "skuId=(\\d+)&";
				String itemCode = getItemCodeReg(url, priceRegex);
				if (priceData.containsKey(itemCode)) {
					Map<String, Object> priceMap = (Map<String, Object>) priceData.get(itemCode);
					if (priceMap.containsKey("stock")) {
						String stock = priceMap.get("stock").toString();
						// 从价格是否为空，判断是否有库存。价格为空-库存无，反之...
						if (stock.equals("") || stock.equals("0")) {
							stockState = false;
						} else {
							stockState = true;
						}

						if (priceMap.containsKey("salePrice")) {
							String salePrice = priceMap.get("salePrice").toString();
							price = Double.parseDouble(salePrice);
						}
					}
				}
			}

			parsedata.put("sh_price", price);
			parsedata.put("sz_price", price);
			parsedata.put("cd_price", price);
			parsedata.put("bj_price", price);

			parsedata.put("sh_stockState", stockState);
			parsedata.put("sz_stockState", stockState);
			parsedata.put("cd_stockState", stockState);
			parsedata.put("bj_stockState", stockState);
		}

		/**
		 * @function 获取促销信息
		 * @note 1、促销信息反爬严重，不分地区分别请求 2、自营标准的json格式，通过json插件获取；非自营为html，通过后处理获取
		 */
		// http://item.feiniu.com/KS1201412CG110000252
		// http://item.feiniu.com/query_detail_page_activity?sku_seq=KS1201412CG110000252
		if (url.contains("query_detail_page_activity")) {
			Map<String, Object> dataSource = (Map<String, Object>) obj;
			if (dataSource.containsKey("data")) {
				Map<String, Object> dataMap = (Map<String, Object>) dataSource.get("data");
				if (dataMap.containsKey("xxhd")) {
					Map<String, Object> xxhd = (Map<String, Object>) dataMap.get("xxhd");
					if (xxhd != null &&xxhd.containsKey("xxhd_data")) {
						Map<String, Object> xxhdData = (Map<String, Object>) xxhd.get("xxhd_data");
						if (xxhdData.containsKey("data")) {
							List<?> promDataList = (List<?>) xxhdData.get("data");
							if (promDataList != null && !promDataList.isEmpty()) {
								List<String> plusBuy = new ArrayList<String>();
								Map<String, Object> promotionMap = new HashMap<String, Object>();
								for (int i = 0; i < promDataList.size(); i++) {
									if (promDataList.get(i) instanceof Map) {
										Map<String, Object> promMap = (Map<String, Object>) promDataList.get(i);
										if (promMap.containsKey("tag")) {
											String tag = promMap.get("tag").toString();
											// 只提取“满减”和“折扣”的促销活动
											if (tag.equals("满减") || tag.equals("折扣")) {
												String title = promMap.get("title").toString();
												plusBuy.add(title);
											}
										}
									}
								}
								
								promotionMap.put("plusBuy", plusBuy);
								parsedata.put("sh_promInfo", promotionMap);
								parsedata.put("bj_promInfo", promotionMap);
								parsedata.put("cd_promInfo", promotionMap);
								parsedata.put("sz_promInfo", promotionMap);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * @param url
	 * @param priceRegex
	 */
	private String getItemCodeReg(String url, String priceRegex) {
		String itemCode = null;
		Matcher match = Pattern.compile(priceRegex).matcher(url);
		if (match.find()) {
			itemCode = match.group(1);
		}
		return itemCode;
	}
}
