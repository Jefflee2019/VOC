package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;
/**
 *	站点：拍拍华为官方旗舰店
 *	作用：处理商品评论
 * @author bfd_05
 *
 */
public class Epaipai_hwCommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(Epaipai_hwCommentJson.class);
	private static final Pattern IID = Pattern.compile("sCmdyId=([\\w+\\d+]*).*?&nCurPage=(\\d+)");
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient arg2, ParseUnit unit) {
			int parsecode = 0;
			Map<String, Object> parseData = new HashMap<String, Object>();
			// 遍历dataList
			for (JsonData jsonData : dataList) {
				if (!jsonData.downloadSuccess()) {
					continue;
				}
				String json = TextUtil.getUnzipJson(jsonData, unit);
				try {
					
					if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
							&& (json.indexOf("[") < json.indexOf("{"))) {
						json = json.substring(json.indexOf("["),
								json.lastIndexOf("]") + 1);
					} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0 && json.indexOf("try{") > 0) {
						
						json = json.substring(json.indexOf("{"),
								json.lastIndexOf("try{") - 1);
					}
					
					executeParse(parseData, json, jsonData.getUrl(), unit);
				} catch (Exception e) {
//					e.printStackTrace();
					parsecode = 500012;
					LOG.warn(
							"JsonParser exception, taskdata url="
									+ taskdata.get("url") + ".jsonUrl :"
									+ jsonData.getUrl(), e);
				}
			}
			// 组装返回结果
			JsonParserResult result = new JsonParserResult();
			try {
				result.setParsecode(parsecode);
				result.setData(parseData);
			} catch (Exception e) {
//				e.printStackTrace();
				LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
			}
			return result;
		}
	
	@SuppressWarnings("unchecked")
	private void executeParse(Map<String, Object> parseData, String json,
			String url, ParseUnit unit) {
		Object obj = null;
		
		try {
			JSONObject jbs = new JSONObject(json);
			obj = JsonUtil.parseObject(jbs.toString());
		} catch (Exception e) {
//			e.printStackTrace();
			LOG.error("Epaipai_hwCommentJson convert json error" );
		}
		Matcher mch = IID.matcher(url);
		String iid = "";
		int pageIndex = 1;
		if(mch.find()){
			iid = mch.group(1);
			pageIndex = Integer.valueOf(mch.group(2));
		}
		String nextpage = "http://shop1.paipai.com/cgi-bin/creditinfo/NewCmdyEval?sCmdyId=%s&nCurPage=%s&nTotal=%s&resettime=1&nFilterType=1&nSortType=11&g_ty=ls";
		if(obj instanceof Map){
			Map<String, Object> data = (Map<String, Object>) obj;
			int goodCommCnt = 0;
			int normalCommCnt = 0;
			int badCommCnt = 0;
			if(data.containsKey("nCommodityGoodNum")){
				goodCommCnt = (Integer)data.get("nCommodityGoodNum");
			}
			if(data.containsKey("nCommodityNormalNum")){
				normalCommCnt = (Integer)data.get("nCommodityNormalNum");
			}
			if(data.containsKey("nCommodityBadNum")){
				badCommCnt = (Integer)data.get("nCommodityBadNum");
			}
			int totalComm = goodCommCnt + normalCommCnt + badCommCnt;
			int totalPage = totalComm%10 == 0 ? totalComm/10 : totalComm/10 + 1;
			List<Map<String, Object>> taskList = null;
			if(parseData.containsKey("tasks")){
				taskList = (List<Map<String, Object>>) parseData.get("tasks");
			}
			else {
				taskList = new ArrayList<Map<String, Object>>();
				parseData.put("tasks", taskList);
			}
			
			if(pageIndex < totalPage){
				nextpage = String.format(nextpage, iid, pageIndex + 1,totalComm);
				Map<String, Object> taskMap = new HashMap<>();
				taskMap.put("link", nextpage);
				taskMap.put("rawlink", nextpage);
				taskMap.put("linktype", "eccomment");
				taskList.add(taskMap);
				parseData.put("nextpage", taskMap);
				parseData.put("task", taskList);
			}
			List<Object> dataList = new ArrayList<>(); //用于存储评论的list
			if(data.containsKey("evalList")){
				List<Map<String, Object>> commList = (List<Map<String, Object>>) data.get("evalList");
				for(Map<String, Object> comm : commList){
					//每页只有十条，但是api返回的这个超过十条，超出的都是null
					if(comm != null){
						initParseData(dataList, comm);
					}
				}
				parseData.put(Constants.COMMENTS, dataList);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void initParseData(List<Object> dataList,
				Map<String, Object> m) {
		Map<String, Object> newMap = new HashMap<String, Object>();
		//评论人
		if(m.containsKey("buyerName")){
			newMap.put(Constants.COMMENTER_NAME, m.get("buyerName"));
		}
		//评论内容
		if(m.containsKey("peerEvalContent")){
			newMap.put(Constants.COMMENT_CONTENT, m.get("peerEvalContent"));
		}
		//评论图片
		if(m.containsKey("smallImgUrl")){
			List<Map<String, Object>> smallImgUrls = (List<Map<String, Object>>) m.get("smallImgUrl");
			List<Map<String, Object>> newImgUrls = new ArrayList<>();
			for(Map<String, Object> smallImgUrl : smallImgUrls){
				if(smallImgUrl != null){
					newImgUrls.add(smallImgUrl);
				}
			}
			newMap.put("contentimgs", newImgUrls);
		}
		//评论人等级
		//页面上没有用户等级，取api用户信用分
//		if(m.containsKey("peerEvalLevel")){
//			newMap.put(Constants.COMMENTER_LEVEL, m.get("peerEvalLevel"));
//		}
		if(m.containsKey("buyerCredit")){
			newMap.put(Constants.BUYER_CREDIT, m.get("buyerCredit"));
		}
		//评论时间
		if(m.containsKey("peerTime")){
			newMap.put(Constants.COMMENT_TIME, m.get("peerTime"));
		}
		if(m.containsKey("commodityProperty")){
			String property =  (String) m.get("commodityProperty");
			String color = property.split("\\|")[0].split(":")[1];
			newMap.put(Constants.COLOR, color);
		}
		dataList.add(newMap);
	}

}
