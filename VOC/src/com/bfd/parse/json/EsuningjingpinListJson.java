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
 * 	站点：苏宁易购
 * 	作用：拼接列表页
 * @author bfd_04
 *
 */
public class EsuningjingpinListJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(EsuningjingpinListJson.class);
	private static final Pattern PAGE_PATTERN1 = Pattern.compile("start=(\\d+)");
	private static final Pattern PAGE_PATTERN2 = Pattern.compile("cp=(\\d+)");
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient normalizerClient,
			ParseUnit unit) {
		int parseCode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
		for (JsonData data : dataList) {
			if (!data.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(data, unit);
//			LOG.info("url:"+data.getUrl()+".json is "+json);
//			LOG.info("goodsList original json:" + json);
			try {
				
//				LOG.info("goodsList prepared json:" + json);
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
						&& (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["),
							json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"),
							json.lastIndexOf("}") + 1);
				}
//				LOG.info("url:"+data.getUrl()+".correct json is "+json);
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
//				e.printStackTrace();
				LOG.warn("json :" + json + ".url:" + taskdata.get("url"));
				LOG.info("goodsList exception during executeParse");
				LOG.warn(
						"AMJsonParser exception, taskdata url="
								+ taskdata.get("url")
								+ ".jsonUrl :" + data.getUrl(), e);
			}

		}
		JsonParserResult result = new JsonParserResult();
		try {
			result.setData(parsedata);
			result.setParsecode(parseCode);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("jsonparser reprocess error url:"+taskdata.get("url"));
		}
		return result;
	}

	/**
	 * execute parse
	 * @param parsedata
	 * @param json
	 * @param url
	 * @param unit
	 */
	public void executeParse(Map<String, Object> parsedata,
			String json, String url, ParseUnit unit){
		try {
			if (url.contains("cshop/queryByKeyword.do")) {
				parseJson1(parsedata, json, url, unit);
			}else if (url.contains("brandquery/brandstoreQuery")) {
				parseJson2(parsedata, json, url, unit);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("executeParse error "+url);
			LOG.info("goodstList error!");
		}
	}
	
	/**
	 * 
	 * @param parsedata
	 * @param json
	 * @param url
	 * @param unit
	 * @throws Exception
	 */
	private void parseJson2(Map<String, Object> parsedata,
			String json, String url, ParseUnit unit) throws Exception{
		Map<String,Object> jsonMap = (Map<String, Object>) JsonUtils.parseObject(json);
		List<Map<String, Object>> goodstList =(List<Map<String, Object>>)jsonMap.get("goodList");
		int totalSize = (Integer) jsonMap.get("totalGoodsCount");
		List<Map<String,Object>> taskList = new ArrayList<Map<String,Object>>();
		parsedata.put("tasks", taskList);
		if(goodstList !=null && !goodstList.isEmpty())
		{
			List<Map<String,Object>> itemList = new ArrayList<Map<String,Object>>();
			for(Map<String,Object> consultItem : goodstList){
				Map<String, Object> reMap = new HashMap<String, Object>();
				Map<String, Object> tempTask = new HashMap<String, Object>();
				
				reMap.put(Constants.ITEMNAME, consultItem.get("catentdesc"));
				reMap.put(Constants.ITEMLINK, "https:" + consultItem.get("commidityUrl"));	
				tempTask.put("link", reMap.get(Constants.ITEMLINK));
				tempTask.put("rawlink", consultItem.get("commidityUrl"));
				tempTask.put("linktype", "eccontent");
				
				taskList.add(tempTask);
				itemList.add(reMap);
			}
			parsedata.put("items",itemList);       //parseResult body
			//处理下一页
			Matcher match = PAGE_PATTERN2.matcher(url);
			if(match.find())
			{
				int pageIndex = Integer.parseInt(match.group(1));
				int totalPage = totalSize / 48;
				if(pageIndex < totalPage) {
					pageIndex += 1;
					String nextPage = url.replaceAll(match.group(0),  "cp=" + pageIndex);
					Map<String, Object> nextpageTask = new HashMap<String, Object>();
					nextpageTask.put("link", nextPage);
					nextpageTask.put("rawlink", nextPage);
					nextpageTask.put("linktype", "eclist");
					taskList.add(nextpageTask);
					parsedata.put("nextpage", nextpageTask);
					parsedata.put("tasks", taskList);
				}
				
			}
		} else {
			LOG.warn("url:" + url + "do not have items");
		}
	}
	
	/**
	 * 
	 * @param parsedata
	 * @param json
	 * @param url
	 * @param unit
	 * @throws Exception
	 */
	private void parseJson1(Map<String, Object> parsedata,
			String json, String url, ParseUnit unit) throws Exception{
		Map<String,Object> jsonMap = (Map<String, Object>) JsonUtils.parseObject(json);
		List<Map<String, Object>> goodstList =(List<Map<String, Object>>)jsonMap.get("goods");
		int totalSize = (Integer) jsonMap.get("totalSize");
		List<Map<String,Object>> taskList = new ArrayList<Map<String,Object>>();
		parsedata.put("tasks", taskList);
		if(goodstList !=null && !goodstList.isEmpty())
		{
			List<Map<String,Object>> itemList = new ArrayList<Map<String,Object>>();
			for(Map<String,Object> consultItem : goodstList){
				Map<String, Object> reMap = new HashMap<String, Object>();
				Map<String, Object> tempTask = new HashMap<String, Object>();
				
				reMap.put(Constants.ITEMNAME, consultItem.get("catentdesc"));
				reMap.put(Constants.ITEMLINK, "https:" + consultItem.get("commidityUrl"));	
				tempTask.put("link", reMap.get(Constants.ITEMLINK));
				tempTask.put("rawlink", consultItem.get("commidityUrl"));
				tempTask.put("linktype", "eccontent");
				
				taskList.add(tempTask);
				itemList.add(reMap);
			}
			parsedata.put("items",itemList);       //parseResult body
			//处理下一页
			Matcher match = PAGE_PATTERN1.matcher(url);
			if(match.find())
			{
				int oldPage = Integer.parseInt(match.group(1));
				int page = oldPage + 48;
				if(totalSize > oldPage) {
					String nextPage = url.replaceAll(match.group(0),  "start=" + page);
					Map<String, Object> nextpageTask = new HashMap<String, Object>();
					nextpageTask.put("link", nextPage);
					nextpageTask.put("rawlink", nextPage);
					nextpageTask.put("linktype", "eclist");
					taskList.add(nextpageTask);
					parsedata.put("nextpage", nextpageTask);
					parsedata.put("tasks", taskList);
				}
				
			}
		} else {
			LOG.warn("url:" + url + "do not have items");
		}
		
	}

}
