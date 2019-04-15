package com.bfd.parse.json;

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
import com.bfd.parse.util.TextUtil;

/**
 * @site：华为荣耀直营店(工商银行融e购)(Eicbc)
 * @function：新闻内容页 Json插件 获取市场价和商城价
 * 
 * @author bfd_02
 */
public class EicbcContentJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(EicbcContentJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient normalizerClient,
			ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
		for (JsonData data : dataList) {
			if (!data.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(data, unit);
			// LOG.info("url:" + data.getUrl() + ".json is " + json);
			try {
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
				LOG.warn("jsonparser reprocess error url:" + taskdata.get("url"));
				parsecode = 500012;
				LOG.warn(
						"AMJsonParser exception, taskdata url="
								+ taskdata.get("url") + ".jsonUrl :"
								+ data.getUrl(), e);
			}
		}
		JsonParserResult result = new JsonParserResult();
		try {
			result.setData(parsedata);
			result.setParsecode(parsecode);
		} catch (Exception e) {
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		try {
			/**
			 * 价格所在页面位置￥<span class="b_link"
			 * id="originPrice priceParam="1,299.00 - ￥1,478.00">1,299.00 - ￥1,478.00</span>"
			 * 
			 */
			//
			Pattern ptn = Pattern
					.compile("id=\"(\\w+)\"\\s+priceParam=\"([\\-\\.,\\d\\s￥]+)\">");
			Matcher match = ptn.matcher(json);
			while (match.find()) {
				String id = match.group(1);
				String priceTemp = match.group(2); // 1,299.00 - ￥1,478.00
				priceTemp = priceTemp.replace("￥", "");
				if (id.equals("originPrice")) {
					// 如果价格是区间表示的，需要拆开按最高和最低价放置
					if (priceTemp.contains("-")) {
						String[] priceArr = priceTemp.split("-");
						parsedata.put(Constants.MARKETLOWERPRICE, priceArr[0]);
						parsedata.put(Constants.MARKETUPPERPRICE, priceArr[1]);
					} else {
						parsedata.put(Constants.MARKETPRICE, priceTemp);
					}
				} else {
					if (priceTemp.contains("-")) {
						String[] priceArr = priceTemp.split("-");
						parsedata.put(Constants.STORELOWERPRICE, priceArr[0]);
						parsedata.put(Constants.STOREUPPERPRICE, priceArr[1]);
					} else {
						parsedata.put(Constants.PRICE, priceTemp);
					}
				}
			}
		} catch (Exception e) {
			LOG.error("parsedata is null");
		}
	}
}