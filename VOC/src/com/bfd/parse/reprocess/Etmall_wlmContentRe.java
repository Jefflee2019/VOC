package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：天猫商城
 * 
 * 主要功能： 1、参数中提取品牌 2、处理价格 3、处理促销 4、处理库存
 * 
 * @author lth
 *
 */
public class Etmall_wlmContentRe implements ReProcessor {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		// 品牌 逗鲜
		if (resultData.containsKey("parameter")) {
			String parameter = resultData.get("parameter").toString();
			String regex = "品牌\\s*:\\s*(\\S*)";
			Matcher match = Pattern.compile(regex).matcher(parameter);
			if (match.find()) {
				String brand = match.group(1);
				resultData.put("brand", brand);
			}
		}

		/**
		 * 价格、促销、库存通过移动端获取
		 * web:https://detail.tmall.com/item.htm?id=557806596358&
		 * skuId=3464849753559
		 * mobile:https://detail.m.tmall.com/item.htm?id=557806596358
		 * &skuId=3464849753559
		 */
		String url = unit.getUrl();
		String mobileUrl = url.replace("detail.tmall", "detail.m.tmall");
		try {
			HttpClientBuilder builder = HttpClientBuilder.create();
			builder.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
			HttpClient client = builder.build();

			HttpGet request = new HttpGet(mobileUrl);
			HttpResponse response = client.execute(request);

			String responseData = EntityUtils.toString(response.getEntity(), "utf-8");
			System.out.println(responseData);
			if (responseData.contains("var _DATA_Mdskip")) {
				String regex = "var _DATA_Mdskip =([\\r\\n\\S\\s]*?)(</script>)";
				Matcher match = Pattern.compile(regex).matcher(responseData);
				if (match.find()) {
					String priceAndStock = match.group(1);
					System.out.println(priceAndStock);
					Map priceAndStockMap = JSON.parseObject(priceAndStock);
					if (priceAndStockMap.containsKey("price")) {
						Map<String, Object> priceMap = (Map) priceAndStockMap.get("price");
						if (priceMap.containsKey("price")) {
							Map<String, Object> priceMap2 = (Map<String, Object>) priceMap.get("price");
							if (priceMap2.containsKey("priceText")) {
								Double price = Double.parseDouble(priceMap2.get("priceText").toString());
								resultData.put("sz_price", price);
								resultData.put("sh_price", price);
								resultData.put("bj_price", price);
								resultData.put("cd_price", price);
							}
						}

						// 促销
						if (priceMap.containsKey("shopProm")) {
							// 存放促销信息
							Map<String, Object> promInfo = new HashMap<String, Object>();
							// 整合促销信息
							List<String> plusBuy = new ArrayList<String>();
							List<Map> shopPromList = (List) priceMap.get("shopProm");
							if (shopPromList != null && !shopPromList.isEmpty()) {
								for (Map shopProm : shopPromList) {
									if (shopProm.containsKey("title")) {
										String title = shopProm.get("title").toString();
										plusBuy.add(title);
									}
								}
								promInfo.put("plusBuy", plusBuy);
								resultData.put("sz_promInfo", promInfo);
								resultData.put("sh_promInfo", promInfo);
								resultData.put("bj_promInfo", promInfo);
								resultData.put("cd_promInfo", promInfo);
							}
						}
					}

					// 评论数
					if (priceAndStockMap.containsKey("item")) {
						Map itemMap = (Map) priceAndStockMap.get("item");
						if (itemMap.containsKey("commentCount")) {
							int replyCnt = Integer.parseInt(itemMap.get("commentCount").toString());
							resultData.put(Constants.REPLY_CNT, replyCnt);
						}
					}

					// 库存
					if (priceAndStockMap.containsKey("skuCore")) {
						Matcher skuidMatch = Pattern.compile("&skuId=(\\d+)").matcher(url);
						Boolean stockStatus = true;
						if (skuidMatch.find()) {
							String skuId = match.group(1);
							Map skuCore = (Map) priceAndStockMap.get("skuCore");
							if (skuCore.containsKey("sku2info")) {
								Map sku2info = (Map) skuCore.get("sku2info");
								if (sku2info.containsKey(skuId)) {
									Map skuIdMap = (Map) sku2info.get(skuId);
									if (skuIdMap.containsKey("quantity")) {
										int quantity = Integer.parseInt(skuIdMap.get("quantity").toString());
										stockStatus = quantity > 0 ? true : false;
									}
								}
							}
						} else {
							Map skuCore = (Map) priceAndStockMap.get("skuCore");
							if (skuCore.containsKey("sku2info")) {
								Map sku2info = (Map) skuCore.get("sku2info");
								if (sku2info.containsKey("0")) {
									Map skuIdMap = (Map) sku2info.get("0");
									if (skuIdMap.containsKey("quantity")) {
										int quantity = Integer.parseInt(skuIdMap.get("quantity").toString());
										stockStatus = quantity > 0 ? true : false;
									}
								}
							}
						}
						resultData.put("sz_stockState", stockStatus);
						resultData.put("sh_stockState", stockStatus);
						resultData.put("bj_stockState", stockStatus);
						resultData.put("cd_stockState", stockStatus);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
