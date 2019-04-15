package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：(京东)沃尔玛官方旗舰店
 * 
 * 主要功能： 1、处理商品路径 2、提取品牌 3、处理价格
 * 
 * @author lth
 *
 */
public class EwartmFlagship_wlmContentRe implements ReProcessor {

//	private static final Log LOG = LogFactory.getLog(EwartmFlagship_wlmContentRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		/**
		 * 获取价格(默认深圳)
		 * http://p.3.cn/prices/mgets?area=19_1607_3155_0&pduid=429459450&skuIds=J_5796655
		 * 2018-01-25 价格不再通过页面请求，而通过列表页关联
		 */
//		String url = unit.getUrl();
//		Matcher itemNoMatch = Pattern.compile("jd.com/(\\d+)").matcher(url);
//		if (itemNoMatch.find()) {
//			String itemNo = itemNoMatch.group(1);
//			String priceUrl = "http://p.3.cn/prices/mgets?area=19_1607_3155_0&type=1&pdtk=&pduid=15163590463631346221332&pdpin=&pin=null&pdbp=0&skuIds=J_".concat(itemNo);
//			HttpClientBuilder builder = HttpClientBuilder.create();
//			builder.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
//			HttpClient client = builder.build();
//			HttpGet request = new HttpGet(priceUrl);
//			request.setHeader("Accept", "*/*");
//			request.setHeader("Accept-Encoding", "gzip, deflate, sdch, br");  
//			request.setHeader("Accept-Language", "zh-CN,zh;q=0.8");  
//			request.setHeader("Connection", "keep-alive");  
//			request.setHeader("Host", "p.3.cn");  
//			request.setHeader("refer", url);  
//			request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36"); 
//			HttpResponse response;
//			try {
//				response = client.execute(request);
//				String priceData = EntityUtils.toString(response.getEntity(), "utf-8");
//				List<Map<String, Object>> data = (List<Map<String, Object>>) com.alibaba.fastjson.JSON.parse(priceData);
//				if (data != null && !data.isEmpty()) {
//					Map<String, Object> priceMap = data.get(0);
//					if (priceMap.containsKey("p")) {
//						Double price = Double.parseDouble(priceMap.get("p").toString());
//						resultData.put("price", price);
//					}
//				}
//				
//			} catch (Exception e) {
//				LOG.error("httprequest download failed"+priceUrl);;
//			} 
//
//		}

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
		 * function:处理cate,将cate分成目录、品牌、产品三部分,并且品牌只取第一个
		 * 处理前："cate":["食品饮料",">","休闲食品",">","糖果/巧克力",">","金冠",">","金冠 黑糖话梅糖160g..."]
		 * 处理后："cate":["食品饮料$休闲食品$糖果/巧克力","金冠","金冠 黑糖话梅糖160g..."
		 */
		if (resultData.containsKey(Constants.CATE)) {
			List<String> cateList = (List<String>) resultData.get(Constants.CATE);
			if (cateList != null && !cateList.isEmpty()) {
				// 去除">"
				Iterator<String> it = cateList.iterator();
				while (it.hasNext()) {
					String st = (String) it.next();
					if (st.equals(">")) {
						it.remove();
					}
				}
				// 将cate分成目录、品牌、产品三部分
				List<String> cate = new ArrayList<String>();
				StringBuffer sb = new StringBuffer();
				// "cate": ["", "","施华蔻","施华蔻男士洗发水薄荷活力洗发露450/200ml控油补水洗头.."]
				// cate 存在如上前部分为空的情况
				if (cateList.size() - 2 > 0) {
					for (int i = 0; i < cateList.size() - 2; i++) {
						if (cateList.get(i).equals("")) {
							continue;
						} else {
							sb.append(cateList.get(i)).append("$");
						}
					}
					if (sb.length() != 0) {
						sb = sb.deleteCharAt(sb.length() - 1);
						cate.add(sb.toString());
					}
					// 重新组装cate
					String brand = cateList.get(cateList.size() - 2);
					String[] brandarr = brand.split("	");
					// //品牌只取第一个
					if (brandarr.length > 1) {
						brand = brandarr[0];
					}
					cate.add(brand);
					cate.add(cateList.get(cateList.size() - 1));
					resultData.put(Constants.CATE, cate);
				}
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	/**
	 * @param url
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	/*private String getHtml(String url) throws IOException, ClientProtocolException {
		HttpClientBuilder builder = HttpClientBuilder.create();
		builder.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
		HttpClient client = builder.build();
		HttpGet request = new HttpGet(url);
		HttpResponse response = client.execute(request);

		String html = EntityUtils.toString(response.getEntity(), "utf-8");
		return html;
	}*/

}
