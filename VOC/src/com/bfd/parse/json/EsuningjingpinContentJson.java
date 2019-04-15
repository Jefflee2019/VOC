package com.bfd.parse.json;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.TextUtil;

/**
 * @site：华为荣耀官方旗舰店(苏宁易购)
 * @function：处理商品价格等
 * @author bfd_04
 *
 */
public class EsuningjingpinContentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(EsuningjingpinContentJson.class);

	@SuppressWarnings("unchecked")
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient normalizerClient, ParseUnit unit) {
		int parseCode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
		for (JsonData data : dataList) {
			if (!data.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(data, unit);
			// LOG.info("url:"+data.getUrl()+".json is "+json);
			// 将ajax数据转化为json数据格式
			if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0 && (json.indexOf("[") < json.indexOf("{"))) {
				json = json.substring(json.indexOf("["), json.lastIndexOf("]") + 1);
			} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
				json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
			}
			try {
				// LOG.info("url:"+data.getUrl()+".correct json is "+json);
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
				 e.printStackTrace();
				LOG.warn("AMJsonParser exception, taskdata url=" + taskdata.get("url") + ".jsonUrl :" + data.getUrl(),
						e);
			}

		}
		JsonParserResult result = new JsonParserResult();
		try {
			result.setData(parsedata);
			// LOG.info("parsedata is "+parsedata);
			result.setParsecode(parseCode);
		} catch (Exception e) {
			// e.printStackTrace();
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}
	
	/**
	 * execute parse
	 * @param parsedata
	 * @param json
	 * @param url
	 * @param unit
	 */
	public void executeParse(Map<String, Object> parsedata, String json, String url, ParseUnit unit){
		try {
			Map<String, Object> jsonMap = (Map<String, Object>) JsonUtils.parseObject(json);
			if (url.contains("pcData")) {
				getPrice(jsonMap, parsedata);
			} else if (url.contains("commodityrLabels")) {
				getImpression(jsonMap, parsedata);
			} else if (url.contains("commodityProperties")) {
				getPropsEvaluate(jsonMap, parsedata);
			} else if (url.contains("review_satisfy")) {
				getUserEvaluate(jsonMap, parsedata);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("executeParse error "+url);
			LOG.info("goodstList error!");
		}
	}

	// get price
	@SuppressWarnings("rawtypes")
	private void getPrice(Map<String, Object> jsonMap, Map<String, Object> parsedata) {
		Map data = (Map) jsonMap.get("data");
		Map price = (Map) data.get("price");
		if (price.containsKey("saleInfo")) {
			List tempList = (List) price.get("saleInfo");
			if (tempList != null && !tempList.isEmpty()) {
				Map saleInfo = (Map) tempList.get(0);
				if (saleInfo.containsKey("netPrice")) {
					String netPrice = saleInfo.get("netPrice").toString();
					parsedata.put(Constants.PRICE, netPrice);
				}
			}
		}
	}

	// get impression
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void getImpression(Map<String, Object> jsonMap, Map<String, Object> parsedata) {
		List<Object> impressionList = null;
		if (jsonMap.containsKey("commodityLabelCountList")) {
			impressionList = (List) jsonMap.get("commodityLabelCountList");
		}
		StringBuilder impSb = new StringBuilder();
		if (impressionList != null && !impressionList.isEmpty()) {
			for (Object obj : impressionList) {
				Map tempMap = (Map) obj;
				if (tempMap.containsKey("labelId")) {
					tempMap.remove("labelId");
				}
				impSb.append(ConstantFunc.transMapToString(tempMap));
				impSb.append(',');
			}
		}
		parsedata.put(Constants.BUYER_IMPRESSION, impSb.toString());
	}

	// get props
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void getPropsEvaluate(Map<String, Object> jsonMap, Map<String, Object> parsedata) {
		List<Object> propsEvaluateList = null;
		if (jsonMap.containsKey("elements")) {
			propsEvaluateList = (List) jsonMap.get("elements");
		}
		StringBuilder sb = new StringBuilder();
		if (propsEvaluateList != null && !propsEvaluateList.isEmpty()) {
			for (Object obj : propsEvaluateList) {
				Map tempMap = (Map) obj;
				sb.append(ConstantFunc.transMapToString(tempMap));
				sb.append(',');
			}
		}
		parsedata.put(Constants.PROPS_EVALUATION, sb.toString());
	}

	// get user_evaluation
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void getUserEvaluate(Map<String, Object> jsonMap, Map<String, Object> parsedata) {
		List<Object> reviewCountsList = null;
		if (jsonMap.containsKey("reviewCounts")) {
			reviewCountsList = (List) jsonMap.get("reviewCounts");
		}
		// parsedata.put(Constants.USER_EVALUATION, reviewCountsList);
		if (reviewCountsList != null && !reviewCountsList.isEmpty()) {
			Map tempMap = (Map) reviewCountsList.get(0);
			String fiveStarCount = "";
			String fourStarCount = "";
			String threeStarCount = "";
			String twoStarCount = "";
			String oneStarCount = "";
			String totalCount = "";
			String picFlagCount = "";
			String againCount = "";
			String bestCount = "";
			String qualityStar = "";

			if (tempMap.containsKey("fiveStarCount")) {
				fiveStarCount = tempMap.get("fiveStarCount").toString();
			}
			if (tempMap.containsKey("fourStarCount")) {
				fourStarCount = tempMap.get("fourStarCount").toString();
			}
			if (tempMap.containsKey("threeStarCount")) {
				threeStarCount = tempMap.get("threeStarCount").toString();
			}
			if (tempMap.containsKey("twoStarCount")) {
				twoStarCount = tempMap.get("twoStarCount").toString();
			}
			if (tempMap.containsKey("oneStarCount")) {
				oneStarCount = tempMap.get("oneStarCount").toString();
			}
			if (tempMap.containsKey("picFlagCount")) {
				picFlagCount = tempMap.get("picFlagCount").toString();
			}
			if (tempMap.containsKey("againCount")) {
				againCount = tempMap.get("againCount").toString();
			}
			if (tempMap.containsKey("bestCount")) {
				bestCount = tempMap.get("bestCount").toString();
			}
			if (tempMap.containsKey("qualityStar")) {
				qualityStar = tempMap.get("qualityStar").toString();
			}
			if (tempMap.containsKey("totalCount")) {
				totalCount = tempMap.get("totalCount").toString();
			}

			double goodRate = (Float.parseFloat(fiveStarCount) + Integer.parseInt(fourStarCount))
					/ Integer.parseInt(totalCount);
			String poorCnt = oneStarCount;
			String generalCnt = String.valueOf(Integer.parseInt(twoStarCount) + Integer.parseInt(threeStarCount));
			String goodCnt = String.valueOf(Integer.parseInt(fiveStarCount) + Integer.parseInt(fourStarCount));
			NumberFormat nt = NumberFormat.getPercentInstance();
			nt.setMinimumFractionDigits(0);
			parsedata.put(Constants.GOOD_RATE, nt.format(goodRate).toString());
			parsedata.put(Constants.GOOD_CNT, Integer.parseInt(goodCnt));
			parsedata.put(Constants.GENERAL_CNT, Integer.parseInt(generalCnt));
			parsedata.put(Constants.POOR_CNT, Integer.parseInt(poorCnt));
			parsedata.put(Constants.REPLY_CNT, Integer.parseInt(totalCount));
			parsedata.put(Constants.WITHPIC_CNT, Integer.parseInt(picFlagCount));
			parsedata.put(Constants.AGAIN_CNT, Integer.parseInt(againCount));
			parsedata.put(Constants.BEST_CNT, Integer.parseInt(bestCount));
			parsedata.put(Constants.AVERAGE, Float.parseFloat(qualityStar));
		}
	}
}
