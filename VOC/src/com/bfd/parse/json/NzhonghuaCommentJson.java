package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;
/**
 * 驱动之家新闻
 * 评论页面动态数据插件
 * 取评论数据
 * @author bfd_05
 *
 */
public class NzhonghuaCommentJson implements JsonParser{
	private static final Log LOG = LogFactory.getLog(NzhonghuaCommentJson.class);
	
	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parseData, String json,
			String url, ParseUnit unit) {
		Object obj = null;
		try {
			obj = JsonUtil.parseObject(json);
		} catch (Exception e) {
//			e.printStackTrace();
			LOG.error("jsonparser reprocess error url:" + url);
		}
		List<Object> dataList = new ArrayList<Object>();//存放评论的list
		parseData.put(Constants.COMMENTS, dataList);
//		评论者用户名，评论时间，评论内容，参与人数，回复人数
		if(obj instanceof Map){
			Map<String, Object> data = (Map<String, Object>) obj;
			List<Map<String, Object>> comList  = (List<Map<String, Object>>)data.get("list");
			for(Map<String, Object> comment : comList){
				initParseData(dataList, comment);
			}
			//评论
			if(data.containsKey("cnum")){
				parseData.put(Constants.REPLY_CNT, data.get("cnum"));
			}
			//参与人数
			if(data.containsKey("dnum")){
				parseData.put(Constants.PARTAKE_CNT, data.get("dnum"));
			}
		}
		List<Map<String,Object>> taskList = new ArrayList<Map<String,Object>>();
		parseData.put("tasks", taskList);
	}
	
	private void initParseData(List<Object> dataList,
			Map<String, Object> m) {
		Map<String, Object> newMap = new HashMap<String, Object>();
		//评论内容
		if(m.containsKey("content")){
			String content = (String) m.get("content");
			newMap.put(Constants.COMMENT_CONTENT, content.trim());
		}
		//评论时间
		if(m.containsKey("createTime")){
			String createTime = m.get("createTime").toString();
			String commTime = ConstantFunc.normalTime(createTime.substring(0, createTime.length()-3));
			newMap.put(Constants.COMMENT_TIME, commTime);
		}
		//评论人名称
		if(m.containsKey("nickName")){
			newMap.put(Constants.USERNAME, m.get("nickName"));
		}
		//评论人ip
		if(m.containsKey("ip")){
			newMap.put(Constants.COMMENTER_IP, m.get("ip"));
		}
		dataList.add(newMap);
	}

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
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"),
							json.lastIndexOf("}") + 1);
				}
				executeParse(parseData, json, jsonData.getUrl(), unit);
			} catch (Exception e) {
//				e.printStackTrace();
				parsecode = 500012;
				LOG.warn(
						"AMJsonParser exception, taskdata url="
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
//			e.printStackTrace();
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}
}
