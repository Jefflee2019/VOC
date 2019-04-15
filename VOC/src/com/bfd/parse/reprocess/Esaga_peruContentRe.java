package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：Esaga_peru
 * 
 * 主要功能：
 * 
 * @author lth
 *
 */
public class Esaga_peruContentRe implements ReProcessor {

	private static final Log LOG = LogFactory.getLog(Esaga_peruContentRe.class); 
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		String pageData = unit.getPageData();
		Document doc = Jsoup.parse(pageData);
		Element divEle = doc.getElementById("fbra_browseMainProduct");
		Elements script = divEle.getElementsByAttributeValue("type", "text/javascript").eq(0);
		for (Element ele : script) {
			Matcher match = Pattern.compile(
					"var\\s*fbra_browseMainProductConfig\\s*=\\s*(.*);\\s*var fbra_browseMainProduct")
					.matcher(ele.data());
			if (match.find()) {
				Map<String,Object> priceMap;
				try {
					priceMap = (Map<String,Object>) JsonUtil.parseObject(match.group(1));
					if (priceMap.containsKey("state")) {
						Map<String,Object> stateMap = (Map<String,Object>) priceMap.get("state");
						if (stateMap.containsKey("product")) {
							Map<String,Object> productMap = (Map<String,Object>) stateMap.get("product");
							String displayName = "";
							String brand = "";
							// 商品名称
							if (productMap.containsKey("displayName")) {
								displayName = productMap.get("displayName").toString();
							}
							if (productMap.containsKey("brand")) {
								brand = productMap.get("brand").toString();
							}
							String itemname = brand + " " + displayName;
							resultData.put("itemname", itemname);

							// 价格
							if (productMap.containsKey("prices")) {
								List<Map<String,Object>> pricesList = (List<Map<String,Object>>) productMap.get("prices");
								for (Map<String,Object> pricedata : pricesList) {
									if (pricedata.containsKey("label")) {
										String label = pricedata.get("label").toString();
										if (label.equals("Internet")) {
											String originalPrice ="";
											if(pricedata.containsKey("formattedLowestPrice")) {
												originalPrice = pricedata.get("formattedLowestPrice").toString();
											}else {
												originalPrice = pricedata.get("originalPrice").toString();
											}String symbol = pricedata.get("symbol").toString();
											 String price = symbol + originalPrice;
											resultData.put("price", price);
										}
										break;
									}
								}
							}
						}
					}
				} catch (Exception e) {
					LOG.error("Esaga_peru:"+" "+unit.getUrl()+"html parse failed");
				}
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
