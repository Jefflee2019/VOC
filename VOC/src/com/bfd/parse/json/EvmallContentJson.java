package com.bfd.parse.json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.TextUtil;

/**
 * @site：华为商城
 * @function：处理商品规格参数
 * @author bfd_04
 *
 */
public class EvmallContentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(EvmallContentJson.class);

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
				// executeParse(parsedata, json, data.getUrl(), unit);
				if (data.getUrl().contains("querySkuParameter")) {
					Map<String, Object> jsonMap = (Map<String, Object>) JsonUtils.parseObject(json);
					if (jsonMap.containsKey("specArgument")) {
						List parmsList = (List) jsonMap.get("specArgument");
						parsedata.put(Constants.PARAMETER, generateParamsStr(parmsList));
					} else {
						parsedata.put(Constants.PARAMETER, "");
					}
				} else if (data.getUrl().contains("queryEvaluateScore")) {
					getEvaluateScore(parsedata, json, data.getUrl(), unit);
				}
			} catch (Exception e) {
				// e.printStackTrace();
				// LOG.warn("json :" + json + ".url:" + taskdata.get("url"));
				LOG.warn("AMJsonParser exception, taskdata url=" + taskdata.get("url") + ".jsonUrl :" + data.getUrl(),
						e);
			}

		}
		JsonParserResult result = new JsonParserResult();
		try {
			result.setData(parsedata);
			// LOG.info("parsedata is "+parsedata);
			// System.err.println(JsonUtils.toJSONString(parsedata));
			result.setParsecode(parseCode);
		} catch (Exception e) {
			// e.printStackTrace();
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

	// 获取用户评价和卖家印象
	public void getEvaluateScore(Map<String, Object> parsedata, String json, String url, ParseUnit unit) {
		try {
			Map<String, Object> jsonMap = (Map<String, Object>) JsonUtils.parseObject(json);
			if (jsonMap.containsKey("avgScore")) {
				parsedata.put(Constants.AVERAGE, jsonMap.get("avgScore"));
			}
			if (jsonMap.containsKey("remarkLabelList")) {
				parsedata.put(Constants.BUYER_IMPRESSION, jsonMap.get("remarkLabelList"));
			}
			if (jsonMap.containsKey("remarkLevelList")) {
				List<Object> evalList = (List) jsonMap.get("remarkLevelList");
				int count = 0;
				int poorCnt = 0;
				int generalCnt = 0;
				int goodCnt = 0;
				for (Object obj : evalList) {
					Map tempMap = (Map) obj;
					if (count == 0) {
						if (tempMap.containsKey("percent")) {
							parsedata.put(Constants.GOOD_RATE, Float.parseFloat(tempMap.get("percent").toString()));
						}
						if (tempMap.containsKey("times")) {
							goodCnt = Integer.parseInt(tempMap.get("times").toString());
							parsedata.put(Constants.GOOD_CNT, goodCnt);
						}
					} else if (count == 1) {
						if (tempMap.containsKey("percent")) {
							parsedata.put(Constants.GENERAL_RATE, Float.parseFloat(tempMap.get("percent").toString()));
						}
						if (tempMap.containsKey("times")) {
							generalCnt = Integer.parseInt(tempMap.get("times").toString());
							parsedata.put(Constants.GENERAL_CNT, generalCnt);
						}
					} else if (count == 2) {
						if (tempMap.containsKey("percent")) {
							parsedata.put(Constants.POOR_RATE, Float.parseFloat(tempMap.get("percent").toString()));
						}
						if (tempMap.containsKey("times")) {
							poorCnt = Integer.parseInt(tempMap.get("times").toString());
							parsedata.put(Constants.POOR_CNT, poorCnt);
						}
					}
					count++;
				}
				parsedata.put(Constants.REPLY_CNT, goodCnt + generalCnt + poorCnt);
				// parsedata.put(Constants.USER_EVALUATION,
				// jsonMap.get("remarkLevelList"));
			}
			// if(jsonMap.containsKey("remarkPerNumLst")) {
			// parsedata.put("remarkPerNumLst", jsonMap.get("remarkPerNumLst"));
			// }

		} catch (Exception e) {
			// e.printStackTrace();
			LOG.error("executeParse error " + url);
		}
	}

	/**
	 * 生成商品参数字符串
	 * 
	 * @param list
	 * @return
	 */
	public String generateParamsStr(List list) {
		StringBuilder sb = new StringBuilder();
		if (list != null && !list.isEmpty()) {
			for (Object obj : list) {
				Map map = (Map) obj;
				sb.append(map.get("name")).append(':').append(map.get("value")).append(',');
			}
		}
		return sb.toString();
	}
}
