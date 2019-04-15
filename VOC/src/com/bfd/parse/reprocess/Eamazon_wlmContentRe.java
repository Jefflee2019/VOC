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

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：亚马逊
 * 
 * 主要功能： 1、参数中提取品牌 2、处理商品路径 3、处理价格4、处理店铺
 * 
 * @author lth
 *
 */
public class Eamazon_wlmContentRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		// 品牌 逗鲜
		if (resultData.containsKey("parameter")) {
			String parameter = resultData.get("parameter").toString();
			String regex = "品牌\\s*(\\S*)";
			Matcher match = Pattern.compile(regex).matcher(parameter);
			if (match.find()) {
				String brand = match.group(1);
				resultData.put("brand", brand);
			}
		}

		/**
		 * @function:处理cate ["食品	›	生鲜	›	蔬菜、水果	›	新鲜水果"]
		 */
		if (resultData.containsKey(Constants.CATE)) {
			List<String> cateList = (List<String>) resultData.get(Constants.CATE);
			ArrayList<String> newcateList = new ArrayList<String>();
			if (cateList != null && !cateList.isEmpty()) {
				String cate = cateList.get(0);
				if (cate.contains("›")) {
					String[] cateArr = cate.split("›");
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < cateArr.length - 2; i++) {
						if (cateArr[i].equals("")) {
							break;
						} else {
							sb.append(cateArr[i].trim()).append("$");
						}
					}
					newcateList.add(sb.toString().substring(0, sb.length() - 1));
					newcateList.add(cateArr[cateArr.length - 2].trim());
					newcateList.add(cateArr[cateArr.length - 1].trim());
				}
				resultData.put(Constants.CATE, newcateList);
			}
			resultData.put(Constants.CATE, newcateList);
		}

		/**
		 * @function 处理价格 "price": "￥126.00",
		 */
		if (resultData.containsKey("price")) {
			double price = 0.0;
			String priceStr = resultData.get("price").toString();
			price = Double.parseDouble(priceStr.replace("￥", ""));
			// 移除旧的价格，分地区给定价格
			resultData.remove("price");
			resultData.put("sz_price", price);
			resultData.put("sh_price", price);
			resultData.put("bj_price", price);
			resultData.put("cd_price", price);
		}
		
		/**
		 * @function 统一平台字段，分地区给定促销信息
		 * 
		 */
		if (resultData.containsKey(Constants.PROMOTIONINFO)) {
			String promotionInfo = resultData.get(Constants.PROMOTIONINFO).toString();
			List<String> plusBuy = new ArrayList<String>();
			Map<String,Object> promInfoMap = new HashMap<String,Object>();
			if (promotionInfo != null) {
				plusBuy.add(promotionInfo);
				promInfoMap.put("plusBuy", plusBuy);
				resultData.put("sh_promInfo", promInfoMap);
				resultData.put("sz_promInfo", promInfoMap);
				resultData.put("bj_promInfo", promInfoMap);
				resultData.put("cd_promInfo", promInfoMap);
			}
		}
		
		
		/**
		 * @function 处理店铺 storename："悠汇园旗舰店【乡镇慎拍，极速热线4006601798】"
		 */
		if (resultData.containsKey("storename")) {
			String storename = resultData.get("storename").toString();
			if (storename.contains("【乡镇慎拍")) {
				storename = storename.substring(0, storename.indexOf("【乡镇慎拍"));
				resultData.put(Constants.STORENAME, storename);
			}
		}

		/**
		 * @function 评论数 "reply_cnt"："16 条商品评论"
		 */
		if (resultData.containsKey("reply_cnt")) {
			String replyCnt = resultData.get("reply_cnt").toString();
			resultData.put(Constants.REPLY_CNT, Integer.parseInt(replyCnt.replace("条商品评论", "").trim()));
		} else {
			resultData.put(Constants.REPLY_CNT, 0);
		}

		/**
		 * @function 处理库存
		 * @note 本地下载库存请求
		 */

		// 库存-深圳
//		String state = "广东";
//		String city = "深圳市";
//		String flag = "sz";
		getstockState(unit, resultData, "广东", "深圳市", "sz");
		
		// 库存-上海
		getstockState(unit, resultData, "上海", "上海市", "sh");
		
		// 库存-北京
		getstockState(unit, resultData, "北京", "北京市", "bj");
		
		// 库存-成都
		getstockState(unit, resultData, "四川", "成都市", "cd");
		
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	/**
	 * @param unit
	 * @param resultData
	 * @param state
	 * @param city
	 * @param flag
	 */
	@SuppressWarnings("unchecked")
	private void getstockState(ParseUnit unit, Map<String, Object> resultData, String state, String city, String flag) {
		String pageData = unit.getPageData();
		Matcher match = Pattern.compile("ue_pti='([^']*)'").matcher(pageData);
		String asinCode = "";
		if (match.find()) {
			asinCode = match.group(1);
		}
		StringBuffer sb = new StringBuffer();
		// 拼接库存请求
		String stockUrl = sb
				.append("https://www.amazon.cn/gp/product/features/dynamic-delivery-message/udp-ajax-handler/get-delivery-message.html?_encoding=UTF-8&deviceType=web&asin=")
				.append(asinCode).append("&preselectedMerchantId=&merchantId=&updateSembu=1&state=").append(state)
				.append("&city=").append(city).append("&district=&useDefaultShippingAddress=0&quantity=1").toString();
		// 下载库存请求响应页面
		String stockData = downUrl(stockUrl);
		boolean stockstate = true;
		if (stockData != null && stockData.contains("availabilityMessage")) {
			Map<String, Object> stockMap = (Map<String, Object>) com.alibaba.fastjson.JSON.parse(stockData);
			String availabilityMessage = stockMap.get("availabilityMessage").toString();
			if (availabilityMessage.contains("现货") || availabilityMessage.contains("有货")) {
				stockstate = true;
			} else {
				stockstate = false;
			}
			resultData.put(flag + "_stockState", stockstate);
		} else {// 页面可能会返回反爬信息
			resultData.put(flag + "_stockState", stockstate);
		}
	}

	/**
	 * 
	 */
	private String downUrl(String url) {
		try {
			HttpClientBuilder builder = HttpClientBuilder.create();
			builder.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0");
			HttpClient client = builder.build();
			
			
			HttpGet request = new HttpGet(url);
			HttpResponse response = client.execute(request);
			
			url = EntityUtils.toString(response.getEntity(),"utf-8");
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return url;
	}
}
