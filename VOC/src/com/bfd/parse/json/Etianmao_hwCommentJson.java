package com.bfd.parse.json;

import java.util.ArrayList;
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
 * 站点名：天猫华为官方旗舰店
 * <P>
 * 主要功能：取得评论相关信息
 * @author bfd_01
 *
 */
public class Etianmao_hwCommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(Etianmao_hwCommentJson.class);
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
	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
		parsedata.put("tasks", taskList);
		try {
			Map<String,Object> map = (Map<String, Object>) JsonUtils.parseObject(json);
			// 有图片评论数，追加评论数
			if (map.containsKey("rateCount")) {
				Map<String,Object> temp = (Map<String,Object>) map.get("rateCount");
				parsedata.put(Constants.WITHPIC_CNT, temp.get("picNum"));
				parsedata.put(Constants.AGAIN_CNT, temp.get("used"));
			}
			// 评论
			List<Map<String,Object>> comments = new ArrayList<Map<String,Object>>();
			if (map.containsKey("rateList")) {
				List<Map<String,Object>> list = (List<Map<String,Object>>) map.get("rateList");
				for (Object obj : list) {
					Map<String,Object> temp = (Map<String,Object>) obj;
					Map<String,Object> comm = new HashMap<String,Object>();
					comm.put(Constants.COLOR, getColor(temp.get("auctionSku")
							.toString()));
					comm.put(Constants.BUY_TYPE,
							getBuyType(temp.get("auctionSku").toString()));
					comm.put(Constants.COMMENTER_NAME,
							temp.get("displayUserNick"));
					comm.put(Constants.COMMENT_IMG,
							getPicUrl(temp.get("pics")));
					comm.put(Constants.COMMENT_CONTENT, temp.get("rateContent"));
					comm.put(Constants.COMMENT_TIME, temp.get("rateDate"));
					comm.put(Constants.COMMENTER_LEVEL,
							temp.get("tamllSweetLevel"));
					comm.put(Constants.COMMENT_REPLY, temp.get("reply"));
					// 追加评论
					if (temp.get("appendComment") !=null &&!((Map<String,Object>)temp.get("appendComment")).isEmpty()) {
						Map<String,Object> appendComment = (Map<String,Object>) temp.get("appendComment");
						comm.put(Constants.APPEND_COMMENT_TIME,
								appendComment.get("commentTime"));
						comm.put(Constants.APPEND_COMMENT_CONTENT,
								appendComment.get("content"));
						comm.put(Constants.APPEND_COMMENT_REPLY,
								appendComment.get("reply"));

					}
					comments.add(comm);
				}
			}
			parsedata.put(Constants.COMMENTS, comments);
			
			// 判断有没有下一页评论
			if (map.containsKey("paginator")) {
				Map<String,Object> temp = (Map<String,Object>)map.get("paginator");
				int page = Integer.valueOf(temp.get("page").toString());
				int lastPage = Integer.valueOf(temp.get("lastPage").toString());
				if (page < lastPage) {
					
					String nextpage = url.split("Page=")[0] + "Page=" + (page+1);
					Map<String, Object> commentTask = new HashMap<String, Object>();
					commentTask.put(Constants.LINK, nextpage);
					commentTask.put(Constants.RAWLINK, nextpage);
					commentTask.put(Constants.LINKTYPE, "eccomment");
					taskList.add(commentTask);
					parsedata.put(Constants.NEXTPAGE, commentTask);
				}
			}
			
		} catch (Exception e) {
			LOG.error(e);
		}
	}

	private String getColor(String sku) {
		String color = sku.split(";")[1];
		return color.replace("机身颜色:", "");
	}
	
	private String getBuyType(String sku) {
		String buyType = sku.split(";")[0];
		return buyType.replace("网络类型:", "");
	}
	
	@SuppressWarnings("unchecked")
	private Object getPicUrl(Object obj) {
		List<String> temp = new ArrayList<String>();
		if (obj instanceof List) {
			List<String> list = (List<String>) obj;
			for (int i = 0; i < list.size(); i++) {
				temp.add("http:" + list.get(i).toString());
			}
			return temp;
		} else if (null != obj && !"".equals(obj)) {
			return "http:" + obj;
		}
		return "";
	}
	
}
