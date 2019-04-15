package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.TextUtil;
/**
 * 站点：Bdgtle
 * 功能：获取评论
 * @author dph 2017年11月30日
 *
 */
public class BdgtleCommentJson implements JsonParser{
	private static final Log LOG = LogFactory.getLog(EthmallListJson.class);
	private static final Pattern PATTERN_PAGE = Pattern.compile("page=\\d+");
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient normalizerClient, ParseUnit unit) {
		Map<String,Object> parsedata = new HashMap<String,Object>(5);
		int parsecode = 0;
		JsonParserResult result = new JsonParserResult();
		for(Object obj : dataList){
			JsonData data = (JsonData) obj;
			if(!data.downloadSuccess()){
				continue;
			}
			//解压缩ajax数据
			String json = TextUtil.getUnzipJson(data, unit);
			if(null == json || json.equals("")){
				return result;
			}
			try{
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
						&& (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["),
							json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"),
							json.lastIndexOf("}") + 1);
				}
				// 执行从json数据中提取自己感兴趣的数据
				executeParse(parsedata,json,unit);
			}catch(Exception e){
				LOG.warn("json:" + json + ".url:" + taskdata.get("url"));
				LOG.warn("AMJsonParse exception,taskdat url="+ taskdata.get("url") 
								+ ".jsonUrl:"+ data.getUrl(), e);
			}
		}
		// 组装返回结果
		try{
			result.setParsecode(parsecode);	
			result.setData(parsedata);
		}catch(Exception e){
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}
	/**
	 * 从json中提取信息
	 * @param parsedata
	 * @param json
	 * @param unit 
	 */
	@SuppressWarnings({ "unchecked" })
	public void executeParse(Map<String, Object> parsedata, String json, ParseUnit unit) {
		JSONObject jsonobj = null;
		JSONArray jsonArray = null;
		try {
			jsonobj = new JSONObject (json);
			jsonArray = new JSONArray(jsonobj.getString("list"));  
			List<Map<String, Object>> itemList =null;
			if(parsedata.get(Constants.ITEMS) != null){
				itemList = (List<Map<String,Object>>) parsedata.get(Constants.ITEMS);					
			}else{
				itemList = new ArrayList<Map<String,Object>>();
			}
			if(jsonArray.length() > 0){
				for(int i = 0;i < jsonArray.length();i++){
					Map<String, Object> commentMap= new HashMap<String,Object>(4);
					Object author = jsonArray.getJSONObject(i).get("author");
					Object message = jsonArray.getJSONObject(i).get("message");
					Object date = jsonArray.getJSONObject(i).get("date");
					date =  ConstantFunc.convertTime((String) date);
					commentMap.put(Constants.AUTHOR, author);
					commentMap.put(Constants.REPLYDATE, date);
					commentMap.put(Constants.REPLYCONTENT, message);
					itemList.add(commentMap);
				}
			}
			String link = null;
			String page = null;
			int nextpage = 0;
			Matcher pageM = PATTERN_PAGE.matcher(unit.getUrl());
			while(pageM.find()){
				page = pageM.group(0);
				page = page.replace("page=", "");
				nextpage = Integer.parseInt(page) + 1;
			}
			String url = unit.getUrl().replaceAll("page=\\d+", "page=");
			link = url + nextpage;
			if(jsonArray.length() < 20){
				link = url + 1;
			}
			Map<String, Object> nextpageMap= new HashMap<String,Object>(4);
			nextpageMap.put(Constants.LINK, link);
			nextpageMap.put(Constants.RAWLINK, link);
			nextpageMap.put(Constants.LINKTYPE, Constants.COMMENT_CONTENT);
			parsedata.put(Constants.NEXTPAGE, nextpageMap);
			parsedata.put(Constants.ITEMS, itemList);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
