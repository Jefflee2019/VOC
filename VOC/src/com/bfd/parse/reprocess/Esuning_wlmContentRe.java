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
 * 站点名：苏宁易购
 * 
 * 主要功能： 1、处理商品路径 2、参数中提取品牌 3、获取促销信息
 * 
 * @author lth
 *
 */
public class Esuning_wlmContentRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		// 品牌
		if (resultData.containsKey("parameter")) {
			String parameter = resultData.get("parameter").toString();
			String regex = "品牌：\\s*(\\S*)";
			Matcher match = Pattern.compile(regex).matcher(parameter);
			if (match.find()) {
				String brand = match.group(1);
				resultData.put("brand", brand);
			}
		}

		/**
		 * @function:处理cate,将cate分成目录、品牌、产品三部分,并且品牌只取第一个 处理前： "cate":[
		 *                                               "奶粉 > 婴幼儿奶粉 > 3段奶粉	2段奶粉	1段奶粉	4段奶粉	5段奶粉	特殊配方	pre段奶粉 > 惠氏(Wyeth)3段奶粉	惠氏Wyeth3段奶粉	美素佳儿Friso3段奶粉	雅培Abbott3段奶粉	爱他美aptamil3段奶粉	雀巢Nestle3段奶粉	美赞臣Mead Jo3段奶粉	伊利3段奶粉	诺优能Nutrilo3段奶粉	贝因美BEINGMA3段奶粉	君乐宝JUNLEBA3段奶粉	a23段奶粉	多美滋Dumex3段奶粉	牛栏Nutrilon3段奶粉	合生元BIOSTIM3段奶粉	完达山3段奶粉	飞鹤FIRMUS3段奶粉	可瑞康Karicar3段奶粉	圣元3段奶粉	雅士利3段奶粉	牛栏COWGATE3段奶粉 > 惠氏(Wyeth)3段奶粉"
		 *                                               ] 处理后： "cate":[
		 *                                               "奶粉$婴幼儿奶粉$3段奶粉","金冠",
		 *                                               "金冠 黑糖话梅糖160g..."]
		 * 
		 */
		if (resultData.containsKey(Constants.CATE)) {
			List<String> cateList = (List<String>) resultData.get(Constants.CATE);
			ArrayList<String> newcateList = new ArrayList<String>();
			if (cateList != null && !cateList.isEmpty()) {
				String cate = cateList.get(0);
				if (cate.contains(">")) {
					String[] cateArr = cate.split(">");
					StringBuffer sb = new StringBuffer();
					if (cateArr.length > 2) {
						for (int i = 0; i < cateArr.length - 2; i++) {
							if (cateArr[i].equals("")) {
								continue;
							} else {
								// 如果单一品类有多个并列，只取第一个
								String element = cateArr[i];
								element = getFirstEle(element);
								sb.append(element.concat("$").trim());
							}
						}
						newcateList.add(sb.toString().substring(0, sb.length() - 1));
						String brand = getFirstEle(cateArr[cateArr.length - 2]).trim();
						newcateList.add(brand);
						newcateList.add(cateArr[cateArr.length - 1].trim());
					}
				}
			}
			resultData.put(Constants.CATE, newcateList);
		}

		/**
		 * @function:获取促销信息
		 * @description：促销信息请求链接，需要从价格请求中提取价格参数进行拼接
		 */
		// 商品页链接 http://product.suning.com/0070160971/602881187.html
		// 促销链接
		// http://icps.suning.com/icps-web/queryExtendedGift/000000000602881187_0070160971_755_7550101_2266.00_1_11_1_3,30_pds__0_.vhtm
		String pageData = unit.getPageData();

		// 000000000602881187
		String partNumber = "";
		if (pageData.contains("partNumber")) {
			String regex = "\"partNumber\":\"(\\d+)\"";
			partNumber = getRegex(pageData, regex);
		}

		// 0070160971
		String vendorCode = "";
		if (pageData.contains("vendorCode")) {
			String regex = "\"vendorCode\":\"(\\d+)\"";
			vendorCode = getRegex(pageData, regex);
		}

		/**
		 * @function 拼接促销信息url
		 */
		// 广东深圳罗湖（755_7550101）
		if (resultData.containsKey("sz_price")&&!resultData.get("sz_price").equals("")) {
//			String flag = "sz";
			Double price = (Double) resultData.get("sz_price");
			StringBuffer sb = new StringBuffer();
			String promUrl = sb.append("http://icps.suning.com/icps-web/queryExtendedGift/").append(partNumber)
					.append("_").append(vendorCode).append("_755_7550101_").append(price)
					.append("_1_11_1_3,30_pds__0_.vhtm").toString();

			// 组装促销信息
//			getPromData(resultData, flag, promUrl);
			getPromData(resultData, promUrl);
		}

		// 上海浦东新区（021_0211301）
		/*if (resultData.containsKey("sh_price")&&!resultData.get("sh_price").equals("")) {
			String flag = "sh";
			Double price = (Double) resultData.get("sh_price");
			StringBuffer sb = new StringBuffer();
			String promUrl = sb.append("http://icps.suning.com/icps-web/queryExtendedGift/").append(partNumber)
					.append("_").append(vendorCode).append("_021_0211301_").append(price)
					.append("_1_11_1_3,30_pds__0_.vhtm").toString();

			// 组装促销信息
			getPromData(resultData, flag, promUrl);
		}

		// 四川成都锦江（028_0280101）
		if (resultData.containsKey("cd_price")&&!resultData.get("cd_price").equals("")) {
			String flag = "cd";
			Double price = (Double) resultData.get("cd_price");
			StringBuffer sb = new StringBuffer();
			String promUrl = sb.append("http://icps.suning.com/icps-web/queryExtendedGift/").append(partNumber)
					.append("_").append(vendorCode).append("_028_0280101_").append(price)
					.append("_1_11_1_3,30_pds__0_.vhtm").toString();

			// 组装促销信息
			getPromData(resultData, flag, promUrl);
		}

		// 北京东城区（010_0100101）
		if (resultData.containsKey("bj_price")&&!resultData.get("bj_price").equals("")) {
			String flag = "bj";
			Double price = (Double) resultData.get("bj_price");
			StringBuffer sb = new StringBuffer();
			String promUrl = sb.append("http://icps.suning.com/icps-web/queryExtendedGift/").append(partNumber)
					.append("_").append(vendorCode).append("_010_0100101_").append(price)
					.append("_1_11_1_3,30_pds__0_.vhtm").toString();

			// 组装促销信息
			getPromData(resultData, flag, promUrl);
		}*/

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	/**
	 * @param element
	 */
	private String getFirstEle(String element) {
		String[] cateEleArr = element.split("	");
		if (cateEleArr.length > 1) {
			element = cateEleArr[0];
		}
		return element;
	}

	/**
	 * @param resultData
	 * @param flag
	 * @param promUrl
	 */
	@SuppressWarnings("unchecked")
	private void getPromData(Map<String, Object> resultData, String promUrl) {
		// 下载促销信息，提取所需内容
		String promData = getHtml(promUrl);

		// 解析json数据
		Map<String, Object> map = (Map<String, Object>) JSON.parse(promData);
		if (map.containsKey("promotions")) {
			List<Map<String, Object>> promotions = (List<Map<String, Object>>) map.get("promotions");
			// 存放促销信息
			Map<String, Object> promInfo = new HashMap<String, Object>();
			// 整合促销信息
			List<String> plusBuy = new ArrayList<String>();
			if (promotions != null && !promotions.isEmpty()) {
				for (Map<String, Object> promMap : promotions) {

					String activityDesc = promMap.containsKey("activityDesc") ? promMap.get("activityDesc").toString()
							: "";
					/*String promotionLabel = promMap.containsKey("promotionLabel") ? promMap.get("promotionLabel")
							.toString() : "";*/
					if (activityDesc.equals("")) {
						continue;
					} else if (!activityDesc.contains("券")) {
						plusBuy.add(activityDesc);
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

	/**
	 * @param pageData
	 * @param regex
	 */
	private String getRegex(String pageData, String regex) {
		String partNumber = null;
		Matcher match = Pattern.compile(regex).matcher(pageData);
		if (match.find()) {
			partNumber = match.group(1);
		}
		return partNumber;
	}

	// 下载页面源码
	public static String getHtml(String url) {
		String html = "";
		try {
			HttpClientBuilder builder = HttpClientBuilder.create();
			builder.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0");
			HttpClient client = builder.build();

			HttpGet request = new HttpGet(url);
			HttpResponse response = client.execute(request);

			html = EntityUtils.toString(response.getEntity(), "utf-8");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return html;
	}
}
