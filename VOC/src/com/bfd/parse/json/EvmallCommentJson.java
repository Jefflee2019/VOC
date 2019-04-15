
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
 * 站点：华为商城 作用：处理商品评论
 * 
 * @author bfd_04
 * 
 */
public class EvmallCommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(EvmallCommentJson.class);
	private static final Pattern PAGE_PATTERN = Pattern
			.compile("pageNumber=(\\d+)");

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient normalizerClient,
			ParseUnit unit) {
		int parseCode = 0;
		String json = null;
		String url = unit.getUrl();
		Map<String, Object> parsedata = new HashMap<String, Object>();
		for (JsonData obj : dataList) {
			if (null != obj) {
				JsonData data = (JsonData) obj;
				if (!data.downloadSuccess()) {
					continue;
				}
				unit.setPageEncode("utf8");
				json = TextUtil.getUnzipJson(data, unit);
				// LOG.info("url:"+data.getUrl()+".json is "+json);
				try {
					// 将ajax数据转化为json数据格式
					if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
							&& (json.indexOf("[") < json.indexOf("{"))) {
						json = json.substring(json.indexOf("["),
								json.lastIndexOf("]") + 1);
					} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
						json = json.substring(json.indexOf("{"),
								json.lastIndexOf("}") + 1);
					}
					// LOG.info("url:"+data.getUrl()+".correct json is "+json);
				} catch (Exception e) {
					LOG.warn("json :" + json + ".url:" + taskdata.get("url"));
					LOG.warn(
							"AMJsonParser exception, taskdata url="
									+ taskdata.get("url") + ".jsonUrl :"
									+ data.getUrl(), e);
				}
			}
			executeParse(parsedata, json, url, unit);
		}
		JsonParserResult result = new JsonParserResult();
		try {
			result.setData(parsedata);
			result.setParsecode(parseCode);
		} catch (Exception e) {
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		try {
			LOG.info("original json is: " + json);
			List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
			parsedata.put("tasks", taskList);
			Map<String, Object> jsonMap = (Map<String, Object>) JsonUtils
					.parseObject(json);
			if(jsonMap.containsKey("success")){
				Boolean flag = (Boolean) jsonMap.get("success");
				if(flag){
					int totalPage = (Integer) ((Map<String, Object>) jsonMap
							.get("page")).get("totalPage");
					int page = (Integer) ((Map<String, Object>) jsonMap
							.get("page")).get("pageNumber");
					parsedata.put("totalPage", totalPage);
					List<Map<String, Object>> remarkList = (List<Map<String, Object>>) jsonMap
							.get("remarkList");
					if (remarkList != null && !remarkList.isEmpty()) {
						List<Map<String, Object>> itemList = new ArrayList<Map<String, Object>>();
						if(page==1) {
							Map secLastItemMap = remarkList.get((remarkList.size()-2)<0?0:(remarkList.size()-2));
							Map firstItemMap = remarkList.get(0);
							remarkList.set(0, secLastItemMap);
							remarkList.set((remarkList.size()-2)<0?0:(remarkList.size()-2), firstItemMap);
						}
						
						for (Map<String, Object> commItem : remarkList) {
							Map<String, Object> reMap = new HashMap<String, Object>();
							reMap.put(Constants.COMMENT_CONTENT,
									commItem.get("content"));
							reMap.put(Constants.COMMENT_TIME,
									commItem.get("createDate"));
							// reMap.put(Constants.CUSTOMER_ID, commItem.get("custId"));
							reMap.put(Constants.COMMENTER_NAME,
									commItem.get("custName"));
							reMap.put(Constants.COMMENTER_LEVEL,
									commItem.get("gradeCode"));
							reMap.put(Constants.SCORE, commItem.get("score"));
							// reMap.put(Constants.COMMENT_ID, commItem.get("id"));
							List<Object> labelList = (List<Object>) commItem.get("labelList");
							StringBuilder sb = new StringBuilder();
							for (Object obj : labelList) {
								sb.append(obj.toString()).append(',');
							}
							reMap.put(Constants.COMMENT_PROPS_EVALUATION, sb.toString());
							reMap.put(Constants.COMMENT_REPLY,
									commItem.get("msgReplyList"));
							// reMap.put(Constants.PRODUCTID,
							// commItem.get("productId"));
							reMap.put(Constants.COMMENT_LEVEL,
									commItem.get("remarkLevel"));
							reMap.put(Constants.SCORE, commItem.get("score"));
							itemList.add(reMap);
						}
						parsedata.put("comments", itemList); // parseResult body
					} 
					nextTask(parsedata, url, page < totalPage);
				}
				else {
					//api返回失败的话在翻一页
					nextTask(parsedata, url, true);
				}
			}
		} catch (Exception e) {
			LOG.error("EvmallCommentJson executeParse error " + url);
			//页面有时候会出现特殊等原因导致返回json转map失败从而丢失下一页
			nextTask(parsedata, url, true);
		}
	}
	
	/**
	 * 组装task
	 * @param parsedata
	 * @param nextPage
	 */
	@SuppressWarnings("unchecked")
	private void initTask(Map<String, Object> parsedata, String nextPage) {
		List<Map<String, Object>> taskList = (List<Map<String, Object>>) parsedata.get("tasks");
		Map<String, Object> nextpageTask = new HashMap<String, Object>();
		nextpageTask.put("link", nextPage);
		nextpageTask.put("rawlink", nextPage);
		nextpageTask.put("linktype", "eccomment");
		
		taskList.add(nextpageTask);
		parsedata.put("nextpage", nextpageTask);
		parsedata.put("tasks", taskList);
	}
	
	/**
	 * 用于下一页
	 * @param parsedata
	 * @param url 本页url
	 * @param flag 判断是否需能生成下一页
	 */
	private void nextTask(Map<String, Object> parsedata, String url, boolean flag){
		Matcher match = PAGE_PATTERN.matcher(url);
		if (match.find() && flag) {
			int page = Integer.parseInt(match.group(1)) + 1;
			String nextPage = url.replaceAll(
					"pageNumber=" + match.group(1), "pageNumber="
							+ page);
			initTask(parsedata, nextPage);
		}
	}
}
