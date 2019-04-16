package com.bfd.parse.json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.TextUtil;
/**
 * 站点名：天猫华为官方旗舰店
 * <P>
 * 主要功能：
 * @author bfd_01
 *
 */
public class Etianmao_hwContentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(Etianmao_hwContentJson.class);
	private static final String COLON = ":";
	private static final String COMMA = ",";
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
			LOG.info("url:" + data.getUrl() + ".json is " + json);
			try {
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
						&& (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["),
							json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"),
							json.lastIndexOf("}") + 1);
				}
				LOG.info("url:" + data.getUrl() + ".correct json is " + json);

				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
				LOG.warn("json :" + json + ".url:" + taskdata.get("url"));
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
			// 收藏数
			if (url.contains("jsonCollection")) {
				getCollectionCnt(url, json, parsedata);
			}
			// 买家印象
			if (url.contains("jsonBuyerImpression")) {
				getImpression(json, parsedata);
			}
			// 累计评价/商品得分
			if (url.contains("jsonReplyCnt")) {
				getReplyCnt(json, parsedata);
			}
			// 月销量
			if (url.contains("mdskip.taobao.com")) {
//				LOG.info("URL :" + url);
				getSellCount(json,parsedata);
			}
			
		} catch (Exception e) {
			LOG.error(e);
		}
	}
	
	/**
	 * 取得收藏数
	 * @param url
	 * @param json
	 * @return
	 */
	private void getCollectionCnt(String url, String json, Map<String, Object> parsedata) {
		String args = url.split(",")[1];
		Pattern iidPatter = Pattern.compile("\"" + args + "\":(\\d+)");
		Matcher match = iidPatter.matcher(json);
		int collectionCnt = 0;
		while (match.find()) {
			collectionCnt = Integer.valueOf(match.group(1));
		}
		 parsedata.put(Constants.COLLECTION_CNT, collectionCnt);
	}
	
	/**
	 * 买家印象
	 * @param json
	 * @param parsedata
	 */
	@SuppressWarnings("unchecked")
	private void getImpression(String json,Map<String, Object> parsedata) {
		List<String> impressionList = null;
		Map<String, Object> map = null;
		try {
			if (json != null) {				
				map = (Map<String, Object>) JsonUtils.parseObject(json);
			}
			if (map.containsKey("tags")) {
				// impressionList =
				// (List)(((Map)map.get("tags")).get("tagClouds"));
				if (map.get("tags") instanceof Map) {
					Map<String,Object> tempMap = (Map<String,Object>) map.get("tags");
					if (tempMap.containsKey("tagClouds")) {
						if (tempMap.get("tagClouds") instanceof List) {
							List<String> tempList = (List<String>) tempMap.get("tagClouds");
							impressionList = tempList;
						}
					}
				}

			}
			if (impressionList!=null && !impressionList.isEmpty()) {				
				StringBuilder impSb = new StringBuilder();
				for (Object obj : impressionList) {
					Map<String,Object> tempMap = (Map<String,Object>) obj;
					if (tempMap.containsKey("tag")) {
						impSb.append(tempMap.get("tag"));
						impSb.append(COLON);
					}
					if (tempMap.containsKey("count")) {
						impSb.append(tempMap.get("count"));
						impSb.append(COMMA);
					}
				}
				parsedata.put(Constants.BUYER_IMPRESSION, impSb.toString());
			}
		} catch (Exception e) {
			LOG.error(e);
		}
	}
	
	/**
	 * 平均分，累计评价
	 * @param json
	 * @param parsedata
	 */
	@SuppressWarnings("unchecked")
	private void getReplyCnt(String json, Map<String, Object> parsedata) {
		double average = 0.0;
		int replyCnt = 0;
		Map<String, Object> map;
		try {
			map = (Map<String, Object>) JsonUtils.parseObject(json);
			if (map.containsKey("dsr")) {
				average = (double) ((Map<String,Object>) map.get("dsr")).get("gradeAvg");
				replyCnt = (int) ((Map<String,Object>) map.get("dsr")).get("rateTotal");
			}
			parsedata.put(Constants.REPLY_CNT, replyCnt);
			parsedata.put(Constants.AVERAGE, average);
		} catch (Exception e) {
			LOG.error(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void getSellCount(String json, Map<String, Object>parsedata) {
		Map <String, Object> map;
		try {
			map = (Map<String, Object>) JsonUtils.parseObject(json);
			if (map.containsKey("defaultModel")) {
				Map<String,Object> defaultModel = (Map<String,Object>) map.get("defaultModel");
				Map<String,Object> sell = (Map<String,Object>)defaultModel.get("sellCountDO");
				int sellCount = (int) sell.get("sellCount");
				parsedata.put(Constants.QUANTITY, sellCount);
			}
		} catch (Exception e) {
			LOG.error(e);
		}
	}
}
