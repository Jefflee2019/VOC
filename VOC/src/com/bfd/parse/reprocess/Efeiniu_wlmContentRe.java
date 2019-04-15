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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：飞牛网
 * 
 * 主要功能： 1、处理商品路径 2、参数中提取品牌 3、获取非自营店铺类促销信息
 * 
 * @author lth
 *
 */
public class Efeiniu_wlmContentRe implements ReProcessor {

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
		 * @function:处理cate,将cate分成目录、品牌、产品三部分,并且品牌只取第一个 
		 * 处理前： "cate":["母婴、童装、玩具 > 孕婴奶粉 > 1段 > 美赞臣(MeadJohnson)1段 > 美赞臣 蓝臻婴儿配方奶粉1段 900g/..."]
		 * 处理后： "cate":["母婴、童装、玩具浴$孕婴奶粉发$1段","美赞臣(MeadJohnson)1段","美赞臣 蓝臻婴儿配方奶粉1段 900g/..."]
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
		 * @function 获取非自营促销
		 * @html格式内容 producturl:http://item.feiniu.com/90301847010
		 * eg:http://item.feiniu.com/promotion/90301847010/204373?guid=&isNeedPromotion=1&isPromotion=0&showPromotion=1
		 */
		String url = unit.getUrl();
		// 过滤自营，此处只处理非自营，自营skuid前有KS
		Matcher match = Pattern.compile("com/(\\d+)").matcher(url);
		if (match.find()) {
			String skuid = match.group(1);
			String pageData = unit.getPageData();
			Matcher match2 = Pattern.compile("\"merchantId\":([^,]*)").matcher(pageData);
			if (match2.find()) {
				String merchantId = match2.group(1);
				StringBuffer sb = new StringBuffer();
				// 拼接促销url
				String promUrl = sb.append("http://item.feiniu.com/promotion/").append(skuid).append("/" + merchantId)
						.append("?guid=&isNeedPromotion=1&isPromotion=0&showPromotion=1").toString();

				try {
					HttpClientBuilder builder = HttpClientBuilder.create();
					builder.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0");
					HttpClient client = builder.build();

					HttpGet request = new HttpGet(promUrl);
					// 执行请求
					HttpResponse response = client.execute(request);
					// 获取响应消息实体
					String promData = EntityUtils.toString(response.getEntity(), "utf-8");
					// 解析促销的html内容
					Document doc = Jsoup.parse(promData);
					Elements ele = doc.getElementsByClass("TextLengthSty");
					String text = ele.text();
					// 提取促销内容
					if (text.contains("立即参加")) {
						String promotion = text.substring(0, text.indexOf("立即参加"));
						List<String> plusBuy = new ArrayList<String>();
						Map<String, Object> promotionMap = new HashMap<String, Object>();
						plusBuy.add(promotion);
						promotionMap.put("plusBuy", plusBuy);
						resultData.put("sh_promInfo", promotionMap);
						resultData.put("bj_promInfo", promotionMap);
						resultData.put("cd_promInfo", promotionMap);
						resultData.put("sz_promInfo", promotionMap);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
