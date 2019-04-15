package com.bfd.parse.json;

import java.util.ArrayList;
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
 * 站点：苏宁易购华为官方旗舰店 作用：处理商品评论和咨询
 * 
 * @author bfd_04
 *
 */
public class Esuning_hwCommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(Esuning_hwCommentJson.class);
	private static final Pattern PAGE_PATTERN_COMMENT = Pattern.compile("total-(\\d+)");
	private static final Pattern PAGE_PATTERN_CONSULT = Pattern.compile("false-(\\d+)-\\d+-\\d+-\\d+");

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
			String url = unit.getUrl();
			try {
				if (url.contains("review.suning.com")) {
					json = json.replace("reviewList(", "");
					json = json.substring(0, json.length() - 1);
					// LOG.info("url:"+data.getUrl()+".correct json is "+json);
					executeCommentParse(parsedata, json, data.getUrl(), unit); // 商品评论
				} else {
					// json = json.replace("getConsultationItem(", "");
					// json =json.substring(0, json.length()-1);
					// //
					// LOG.info("url:"+data.getUrl()+".correct json is "+json);
					// executeConsultParse(parsedata, json, data.getUrl(),
					// unit);
					parseCode = 500009;
				}
			} catch (Exception e) {
				// e.printStackTrace();
				LOG.warn("json :" + json + ".url:" + taskdata.get("url"));
				LOG.warn("AMJsonParser exception, taskdata url=" + taskdata.get("url") + ".jsonUrl :" + data.getUrl(),
						e);
			}

		}
		JsonParserResult result = new JsonParserResult();
		try {
			// System.err.println("parsedata: " +
			// JsonUtils.toJSONString(parsedata));
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
	 * 处理商品评论
	 * 
	 * @param parsedata
	 * @param json
	 * @param url
	 * @param unit
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void executeCommentParse(Map<String, Object> parsedata, String json, String url, ParseUnit unit) {
		List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
		parsedata.put("tasks", taskList);
		try {
			Map<String, Object> jsonMap = (Map<String, Object>) JsonUtils.parseObject(json);
			List<Map<String, Object>> remarkList = (List<Map<String, Object>>) jsonMap.get("commodityReviews");
			if (remarkList != null && !remarkList.isEmpty()) {
				List<Map<String, Object>> itemList = new ArrayList<Map<String, Object>>();
				Matcher match = PAGE_PATTERN_COMMENT.matcher(url);
				for (Map<String, Object> commItem : remarkList) {
					Map<String, Object> reMap = new HashMap<String, Object>();
					reMap.put(Constants.COMMENT_CONTENT, commItem.get("content"));
					reMap.put(Constants.COMMENT_TIME, commItem.get("publishTime"));
					// reMap.put("commodityReviewId",
					// commItem.get("commodityReviewId"));
					// reMap.put("deviceType", commItem.get("deviceType"));
					reMap.put(Constants.SOURCESYSTEM, commItem.get("sourceSystem"));

					Map userinfoMap = (Map) commItem.get("userInfo");
					reMap.put(Constants.COMMENTER_NAME, userinfoMap.get("nickName"));
					reMap.put(Constants.COMMENTER_LEVEL, userinfoMap.get("levelName"));
					reMap.put(Constants.COMMENTER_IMG, userinfoMap.get("imgUrl"));

					reMap.put(Constants.SCORE, commItem.get("qualityStar"));
					// reMap.put("bestFlag", commItem.get("bestFlag"));
					reMap.put(Constants.SHOPTNAME, ((Map) commItem.get("shopInfo")).get("shopName"));
					// reMap.put("contentLength",
					// commItem.get("contentLength"));

					Map commodityMap = (Map) commItem.get("commodityInfo");
					reMap.put(Constants.COLOR, commodityMap.get("charaterDesc1"));
					reMap.put(Constants.BUY_TYPE, commodityMap.get("charaterDesc2"));

					// reMap.put("againFlag", commItem.get("againFlag"));
					List propsEvaList = (List) commItem.get("individualObject");
					StringBuilder propsSB = new StringBuilder();
					if (!propsEvaList.isEmpty()) {
						for (Object obj : propsEvaList) {
							Map tempMap = (Map) obj;
							propsSB.append(tempMap.get("proptyName")).append(':')
									.append(tempMap.get("proptyDesc"));
							propsSB.append(',');
						}
					}
					reMap.put(Constants.COMMENT_PROPS_EVALUATION, propsSB.toString());

					List labelsList = (List) commItem.get("labelNames");
					StringBuilder labelSB = new StringBuilder();
					if (!labelsList.isEmpty()) {
						for (Object obj : labelsList) {
							Map labelMap = (Map) obj;
							labelSB.append(labelMap.get("labelName")).append(',');
						}
					}
					reMap.put(Constants.COMMENT_TAG, labelSB.toString());
					// reMap.put("score", commItem.get("score"));
					reMap.put(Constants.UP_CNT, commItem.get("usefulCnt"));
					// reMap.put("replyFlag", commItem.get("replyFlag"));
					reMap.put(Constants.COMMENT_REPLY, commItem.get("returnMsg"));
					itemList.add(reMap);
				}
				parsedata.put("comments", itemList); // parseResult body
				parsedata.put("msg_type", "comment");
				if (!remarkList.isEmpty() && match.find()) {
					int page = Integer.parseInt(match.group(1)) + 1;
					String nextPage = url.replaceAll("total-" + match.group(1), "total-" + page);
					Map<String, Object> nextpageTask = new HashMap<String, Object>();
					nextpageTask.put("link", nextPage);
					nextpageTask.put("rawlink", nextPage);
					nextpageTask.put("linktype", "eccomment");
					taskList.add(nextpageTask);
					parsedata.put("nextpage", nextpageTask);
					parsedata.put("tasks", taskList);
				}
				// LOG.info("url:" + url + "parsedata is " +
				// JsonUtils.toJSONString(parsedata));
			} else {
				LOG.warn("url:" + url + "do not have comment");
			}
		} catch (Exception e) {
			// e.printStackTrace();
			LOG.error("executeParse error " + url);
		}
	}

	/**
	 * convert consultation type
	 * 
	 * @param intType
	 * @return
	 */
	public static String calConsltType(int intType) {
		switch (intType) {
		case 5:
			return "产品咨询";
		case 6:
			return "库存配送";
		case 7:
			return "发票保修";
		case 8:
			return "支付信息";
		case 9:
			return "促销优惠";
		case 10:
			return "其他问题";
		default:
			return "未知";
		}
	}

	/**
	 * execute parse
	 * 
	 * @param parsedata
	 * @param json
	 * @param url
	 * @param unit
	 */
	@SuppressWarnings("unchecked")
	public void executeConsultParse(Map<String, Object> parsedata, String json, String url, ParseUnit unit) {
		List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
		parsedata.put("tasks", taskList);
		try {
			Map<String, Object> jsonMap = (Map<String, Object>) JsonUtils.parseObject(json);
			List<Map<String, Object>> consultList = (List<Map<String, Object>>) jsonMap.get("consulationList");
			int totalPage = (Integer) jsonMap.get("totalCount");
			if (consultList != null && !consultList.isEmpty()) {
				List<Map<String, Object>> itemList = new ArrayList<Map<String, Object>>();
				Matcher match = PAGE_PATTERN_CONSULT.matcher(url);
				for (Map<String, Object> consultItem : consultList) {
					Map<String, Object> reMap = new HashMap<String, Object>();
					// reMap.put(Constants.CONSULT_ID,
					// consultItem.get("articleId"));
					reMap.put(Constants.CONSULTER_NAME, consultItem.get("nickname"));
					// reMap.put(Constants.CONSULT_TYPE,
					// calConsltType(Integer.parseInt(
					// consultItem.get("modeltype").toString())));
					reMap.put(Constants.CONSULT_CONTENT, consultItem.get("content"));
					reMap.put(Constants.COMMENT_TIME, consultItem.get("createtime"));
					reMap.put(Constants.CONSULT_REPLYS, consultItem.get("answer"));
					reMap.put(Constants.SUPPLIERNAME, consultItem.get("suppliername"));
					reMap.put(Constants.UP_CNT, consultItem.get("usefulcount"));
					reMap.put(Constants.DOWN_CNT, consultItem.get("unusefulcount"));
					itemList.add(reMap);
				}
				parsedata.put("consults", itemList); // parseResult body
				parsedata.put("msg_type", "consultation");
				if (match.find()) {
					int page = Integer.parseInt(match.group(1)) + 1;
					if (page < totalPage) {
						String nextPage = url.replaceAll("false-" + match.group(1), "false-" + String.valueOf(page));
						Map<String, Object> nextpageTask = new HashMap<String, Object>();
						nextpageTask.put("link", nextPage);
						nextpageTask.put("rawlink", nextPage);
						nextpageTask.put("linktype", "eccomment");
						taskList.add(nextpageTask);
						parsedata.put("nextpage", nextpageTask);
						parsedata.put("tasks", taskList);
					}

				}
				// LOG.info("url:" + url + "parsedata is " +
				// JsonUtils.toJSONString(parsedata));
			} else {
				LOG.warn("url:" + url + "do not have consulatation");
			}
		} catch (Exception e) {
			// e.printStackTrace();
			LOG.error("executeParse error " + url);
		}
	}

}
