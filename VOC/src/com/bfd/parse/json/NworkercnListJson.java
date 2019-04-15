package com.bfd.parse.json;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;
/**
 * 站点名：中工网
 * <p>
 * 主要功能：列表页json数据
 * @author bfd_04
 *
 */
public class NworkercnListJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NworkercnListJson.class);
	
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient normalizerClient,
			ParseUnit unit) {
		Map<String, Object> parsedata = new HashMap<String, Object>();
		List<Map<String,Object>> taskList = new ArrayList<Map<String,Object>> ();
		parsedata.put("tasks", taskList);
		int parsecode = 0;
		for (JsonData data : dataList) {
			if (!data.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(data, unit);
			try {
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
						&& (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["),
							json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"),
							json.lastIndexOf("}") + 1);
				}
//				LOG.info("url:" + data.getUrl() + ".correct json is " + json);
				executeParse(parsedata, json, unit.getUrl(), initSiteMap());
			} catch (Exception e) {
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
			String url, Map<String, Object> siteMap) {
		Object obj = null;
		try {
			obj = JsonUtil.parseObject(json);
		} catch (Exception e) {
			LOG.error("jsonparser reprocess error url:" + url);
		}
		List<Map<String,Object>> taskList = new ArrayList<Map<String,Object>>();
		parsedata.put("tasks", taskList);
		if(obj instanceof Map){
			Map<String, Object> map = (Map<String, Object>) obj;
			List<Map<String, Object>> itemList = new ArrayList<Map<String, Object>>();
			if(map.containsKey("response")){
				Map<String, Object> resMap = (Map<String, Object>) map.get("response");
				List<Map<String, Object>> list = (List<Map<String, Object>>) resMap.get("docs");
				int total = Integer.valueOf(resMap.get("numFound").toString());
				parsedata.put(Constants.ITEMS, itemList);
				for(Map<String, Object> comm : list){
					Map<String, Object> storeMap = new HashMap<String, Object>();
					Map<String, Object> taskMap = new HashMap<String, Object>();
					if(comm.containsKey("title")){
						String title = comm.get("title").toString();
						storeMap.put(Constants.TITLE, title);
					}
					if(comm.containsKey("htmlurl") && comm.containsKey("childwebid")){
						String htmlUrl = "";
						String id = comm.get("childwebid").toString();
						if(siteMap.containsKey(id)){
							htmlUrl = new StringBuilder("http://")
							.append(siteMap.get(id))
							.append(".workercn.cn")
							.append(comm.get("htmlurl")).toString();
						}else{
							htmlUrl = new StringBuilder("http://")
							.append(id)
							.append(".workercn.cn")
							.append(comm.get("htmlurl")).toString();
					}
						storeMap.put("link", htmlUrl);
						taskMap.put(Constants.LINK, htmlUrl);
						taskMap.put(Constants.RAWLINK, htmlUrl);
						taskMap.put(Constants.LINKTYPE, "newscontent");
						taskList.add(taskMap);
					}
					
					itemList.add(storeMap);
				}
				//下一页问题
				initNextTask(taskList, url, total);
			}
		}
	}
	
	private void initNextTask(List<Map<String,Object>> taskList, String url, int total){
		int start = url.indexOf("start=") + 6;
		int rows = url.indexOf("rows=") + 5;
		if((start + rows) < total){
			int nextStart = Integer.valueOf(url.substring(start, url.indexOf("&", start)));
			int nextRows  = Integer.valueOf(url.substring(rows, url.indexOf("&", rows)));
			String keyword = url.substring(url.indexOf("q=") + 2, url.indexOf("&", url.indexOf("q=")));
			StringBuilder sb = new StringBuilder("http://search.workercn.cn/searchservice/info/select?");
			sb.append("start=");
			sb.append(nextStart + 20);
			sb.append("&rows=");
			sb.append(nextRows + 20);
			sb.append("&q=");
			sb.append(keyword);
			sb.append("&sort=releasetime%20desc&wt=json");
			Map<String, Object> taskMap =  new HashMap<String, Object>();
			taskMap .put(Constants.LINK, sb.toString());
			taskMap.put(Constants.RAWLINK, sb.toString());
			taskMap.put(Constants.LINKTYPE, "newslist");
			taskList.add(taskMap);
		}
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> initSiteMap(){
		String url = "http://search.workercn.cn/search/app-info/script/default.js"; 
		Map<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("url", url);
		paraMap.put("User-Agent", "Mozilla/5.0");
		paraMap.put("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		paraMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		Map<String, Object> siteMap = new HashMap<>();
		try {
			String rtStr = getPageData(paraMap, "UTF-8");
			rtStr = rtStr.substring(rtStr.indexOf("var sites"), rtStr.indexOf(";", rtStr.indexOf("var sites")));
			rtStr = rtStr.replace("var sites=", "");
			Object	obj = JsonUtil.parseObject(rtStr);
			if(obj instanceof Map){
				siteMap = (Map<String, Object>) obj;
			}
		} catch (Exception e) {
			LOG.error("jsonparser reprocess error url:" + url);
		}
		return siteMap;
	}
	
	 private String getPageData(Map<String, String> paraMap, String codeType)
				throws Exception {
			String rtPageData = "";
			if (!paraMap.containsKey("url")) {
				return rtPageData;
			}
			String url = paraMap.get("url");
			StringBuffer response = new StringBuffer();

			URL urlObj = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
			conn.setRequestMethod("GET");
			conn.setUseCaches(false);

			Set<String> keySet = paraMap.keySet();
			for (String key : keySet) {
				conn.setRequestProperty(key, paraMap.get(key));
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(
					conn.getInputStream(), codeType));

			String inputLine = "";
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			rtPageData = response.toString();
			return rtPageData;
		}
}
