package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.TextUtil;

public class NdgtleListJson implements JsonParser{
	
	private static final Log LOG = LogFactory.getLog(NdgtleListJson.class);
	private static final Pattern PATTERN_ITEM = Pattern.compile("<item>(.|\n)*?</item>");
	private static final Pattern PATTERN_TITLE = Pattern.compile("<subject>((.|\n)*?)</subject>");
	private static final Pattern PATTERN_TOTAL = Pattern.compile("<total>((.|\n)*?)</total>");
	private static final Pattern PATTERN_PID = Pattern.compile("<tid>(\\d+)</tid>");
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
			try {
				/*if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0 && (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["), json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
				}*/
//				LOG.info("111111111111111111" + json);
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
				// e.printStackTrace();
				LOG.warn("json :" + json + ".url:" + taskdata.get("url"));
				LOG.info("goodsList exception during executeParse");
				LOG.warn("AMJsonParser exception, taskdata url=" + taskdata.get("url") + ".jsonUrl :" + data.getUrl(),
						e);
			}

		}
		JsonParserResult result = new JsonParserResult();
		try {
			result.setData(parsedata);
			result.setParsecode(parseCode);
		} catch (Exception e) {
			// e.printStackTrace();
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}
	
	
	/**
	 * 处理新闻评论
	 * @param parsedata
	 * @param json
	 * @param url
	 * @param unit
	 */
	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parseData,
			String json, String url, ParseUnit unit){
		List<Map<String, Object>> taskList = null;
		taskList = new ArrayList<Map<String, Object>>();
		parseData.put("tasks", taskList);
		Matcher itemM = PATTERN_ITEM.matcher(json);
		String link_tmp = "http://www.dgtle.com/thread-pid-1-1.html";
		List<Map<String, Object>> itemList = new ArrayList<Map<String, Object>>();
		while(itemM.find()){
			String item = itemM.group(0);
			String title = null;
			//TITLE
			Matcher messageM = PATTERN_TITLE.matcher(item);
			if(messageM.find()){
				title = messageM.group(1);
			}
			//link
			Matcher pidM = PATTERN_PID.matcher(item);
			if(pidM.find()){
				String pid = pidM.group(1);
				Map comment = new HashMap();//
				//http://www.dgtle.com/thread-827825-1-1.html
				String link = link_tmp.replaceAll("pid", pid);
				
				Map<String, Object> reMap = new HashMap<String, Object>();
				Map<String, Object> tempTask = new HashMap<String, Object>();
				reMap.put(Constants.TITLE, title);
				reMap.put(Constants.ITEMLINK, link);

				tempTask.put("link", reMap.get(Constants.ITEMLINK) + "?a=a");
				tempTask.put("rawlink", reMap.get(Constants.ITEMLINK) + "?a=a");
				tempTask.put("linktype", "newscontent");

				taskList.add(tempTask);
				itemList.add(reMap);
			}
			
		}
		parseData.put("items", itemList);
		//下一页
//		https://api.yii.dgtle.com/v2/forum-thread/thread?page=1&perpage=24&typeid=18
		Matcher totalM = PATTERN_TOTAL.matcher(json);
		if(totalM.find()){
			String total = totalM.group(1);
			int page_count = Integer.parseInt(total);//总页数
			String index = getCresult(url, "page=(\\d+)&perpage");
			if (page_count > Integer.parseInt(index)) {
				//拼接下一页链接
				String offset = Integer.parseInt(index) + 1 + "";
				String nextPage = url.replaceAll(getCresult1(url, "page=(\\d+)&perpage"), "page="+offset+"&perpage");
				initTask(parseData, nextPage);
			}
		}
	}
	/**
	 * 正则匹配字符串
	 * @param str
	 * @param pattern
	 * @return
	 */
	private String getCresult(String str,String reg){
		Pattern pattern = Pattern.compile(reg);
		Matcher mch = pattern.matcher(str);
		if(mch.find()){
			return mch.group(1);
		}
		return str;
	}
	/**
	 * 正则匹配字符串
	 * @param str
	 * @param pattern
	 * @return
	 */
	private String getCresult1(String str,String reg){
		Pattern pattern = Pattern.compile(reg);
		Matcher mch = pattern.matcher(str);
		if(mch.find()){
			return mch.group(0);
		}
		return str;
	}
	/**
	 * 转换时间戳
	 * @param timestampString
	 * @return
	 */
	private String TimeStamp2Date(String timestampString){  
	    Long timestamp = Long.parseLong(timestampString)*1000;  
	    String date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(timestamp));  
	    return date;  
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
		nextpageTask.put("linktype", "newslist");
		
		taskList.add(nextpageTask);
		parsedata.put("nextpage", nextpageTask);
		parsedata.put("tasks", taskList);
	}
}
