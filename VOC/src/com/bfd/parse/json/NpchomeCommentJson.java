package com.bfd.parse.json;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * 
 * 主要功能：获取评论信息
 * 
 * @author bfd_05
 */
public class NpchomeCommentJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(NpchomeCommentJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient urlnormalizerClients, ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parseData = new HashMap<String, Object>();
		// 遍历dataList
		for (JsonData jsonData : dataList) {
			if (!jsonData.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(jsonData, unit);
			try {
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0 && (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["), json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
				}
				executeParse(parseData, json, jsonData.getUrl(), unit);
			} catch (Exception e) {
				// e.printStackTrace();
				parsecode = 500012;
				LOG.warn(
						"JsonParser exception, taskdata url=" + taskdata.get("url") + ".jsonUrl :" + jsonData.getUrl(),
						e);
			}
		}
		// 组装返回结果
		JsonParserResult result = new JsonParserResult();
		try {
			result.setParsecode(parsecode);
			result.setData(parseData);
		} catch (Exception e) {
			// e.printStackTrace();
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parseData, String json, String url, ParseUnit unit) {
		Object obj = null;
		try {
			obj = JsonUtil.parseObject(json);
		} catch (Exception e) {
			// e.printStackTrace();
			LOG.error("jsonparser reprocess error url:" + url);
		}
		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
		parseData.put(Constants.COMMENTS, dataList);
		if (obj instanceof Map) {
			Map<String, Object> dataMap = (Map<String, Object>) obj;
			List<Map<String, Object>> comments = new ArrayList<Map<String, Object>>();
			String topicID = "";
			if (dataMap.containsKey("listData")) {
				Map<String, Object> listData = ((Map<String, Object>) dataMap.get("listData"));
				// 回复数
				parseData.put(Constants.REPLY_CNT, listData.get("cmt_sum"));
				comments = (List<Map<String, Object>>) listData.get("comments");
				if (listData.get("topic_id") != null) {
					topicID = listData.get("topic_id").toString();
				}
			} else if (dataMap.containsKey("comments")) {
				comments = (List<Map<String, Object>>) dataMap.get("comments");
				parseData.put(Constants.REPLY_CNT, dataMap.get("cmt_sum"));
				if (dataMap.get("topic_id") != null) {
					topicID = dataMap.get("topic_id").toString();
				}
			}
			if (!comments.isEmpty()) {
				for (Map<String, Object> comm : comments) {
					initParseData(dataList, comm);
				}
				// 当网站评论无法读出时，不能根据回复数来判断是否有下一页（回复数有，而评论没有）
				// 需要先判断是否有回复
				getNextpageUrl(parseData, unit, topicID, tasks);
			}
		}
		List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
		parseData.put("tasks", taskList);
	}

	@SuppressWarnings("unchecked")
	private void initParseData(List<Map<String, Object>> dataList, Map<String, Object> comm) {
		Map<String, Object> storeMap = new HashMap<String, Object>();
		Map<String, Object> newMap = new HashMap<String, Object>();
		// 评论人名称
		if (comm.containsKey("passport")) {
			if (((Map<String, Object>) comm.get("passport")).containsKey("nickname")) {
				newMap.put(Constants.USERNAME, ((Map<String, Object>) comm.get("passport")).get("nickname"));
			}

		}
		// 评论内容
		if (comm.containsKey("content")) {
			newMap.put(Constants.COMMENT_CONTENT, comm.get("content"));
		}
		// 评论时间
		if (comm.containsKey("create_time")) {
			long createTime = (long) comm.get("create_time");
			String commentTime = ConstantFunc.normalTime(String.valueOf(createTime / 1000));
			newMap.put(Constants.COMMENT_TIME, commentTime);
		}
		// 引用的评论
		if (comm.containsKey("comments") && !((List<Object>) comm.get("comments")).isEmpty()) {

			List<Map<String, Object>> referComms = (List<Map<String, Object>>) comm.get("comments");
			Map<String, Object> storeReferComm = new HashMap<>();
			Map<String, Object> referComm = referComms.get(referComms.size() - 1);
			Map<String, Object> referPassport = (Map<String, Object>) referComm.get("passport");
			if (referPassport.containsKey("nickname")) {
				storeReferComm.put(Constants.REFER_COMM_USERNAME, referPassport.get("nickname"));
			}
			// 时间
			if (referComm.containsKey("create_time")) {
				Long createTime = (long) referComm.get("create_time");
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				storeReferComm.put(Constants.REFER_COMM_TIME, sdf.format(new Date(createTime)));
			}
			// 评论
			if (referComm.containsKey("content")) {
				storeReferComm.put(Constants.REFER_COMM_CONTENT, referComm.get("content"));
			}
			storeMap.put(Constants.REFER_COMMENTS, storeReferComm);
		}
		// 所在地
		if (comm.containsKey("ip_location")) {
			newMap.put(Constants.CITY, comm.get("ip_location"));

		}
		// ip
		if (comm.containsKey("ip")) {
			newMap.put(Constants.COMMENTER_IP, comm.get("ip"));
		}
		dataList.add(newMap);
	}

	private void getNextpageUrl(Map<String, Object> parseData, ParseUnit unit, String topicID,
			List<Map<String, Object>> taskList) {
		// http://changyan.sohu.com/api/2/topic/comments?client_id=cyqE875ep&topic_id=834228496&page_size=10&page_no=1
		String url = (String) unit.getUrl();
		String clientID = "";
		int pageNo = 1;
		Pattern pattern = Pattern.compile("client_id=(\\w+)");
		Matcher mch = pattern.matcher(url);
		if (mch.find()) {
			clientID = mch.group(1);
		}
		pattern = Pattern.compile("page_no=(\\d+)");
		mch = pattern.matcher(url);
		if (mch.find()) {
			pageNo = Integer.valueOf(mch.group(1));
		}
		if (!clientID.equals("") && !clientID.equals("")) {
			Map<String, Object> nexpageMap = new HashMap<String, Object>();

			// 若当前页面评论数小于页面的评论总数，则不取下一页
			int comReplyCnt = (Integer) parseData.get(Constants.REPLY_CNT);
			int pageSize = comReplyCnt % 10 == 0 ? comReplyCnt % 10 : comReplyCnt / 10 + 1;
			if (pageSize > pageNo) {
				String nextpageUrl = "http://changyan.sohu.com/api/2/topic/comments?client_id=%s&topic_id=%s&page_size=10&page_no=%s";
				nextpageUrl = String.format(nextpageUrl, clientID, topicID, pageNo + 1);
				nexpageMap.put(Constants.LINK, nextpageUrl);
				nexpageMap.put(Constants.RAWLINK, nextpageUrl);
				nexpageMap.put(Constants.LINKTYPE, "newscomment");
				taskList.add(nexpageMap);
				parseData.put(Constants.NEXTPAGE, nexpageMap);
			}
		}
	}
}
