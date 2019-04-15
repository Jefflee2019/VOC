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
public class Esuning_hwListJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(Esuning_hwListJson.class);
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
//			LOG.info("parsedata is "+parsedata);
			result.setParsecode(parseCode);
//			LOG.info("goodsList parseResult is "+parsedata);
		} catch (Exception e) {
//			e.printStackTrace();
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
		Map<String, Object> jsonMap = null;
		try {
			jsonMap = (Map<String, Object>) JsonUtils.parseObject(json);
		} catch (Exception e) {
			// e.printStackTrace();
			LOG.error("executeParse error " + e.getCause().getMessage());
		}
		List<Map<String, Object>> goodstList =(List<Map<String, Object>>)jsonMap.get("goodList");
		List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
		parsedata.put("tasks", taskList);
		List<Map<String, Object>> itemList = new ArrayList<Map<String, Object>>();

		for (Map<String, Object> resultItem : goodstList) {
			Map<String, Object> reMap = new HashMap<String, Object>();
			Map<String, Object> tempTask = new HashMap<String, Object>();
			String name = resultItem.get("catentdesc").toString();
			String route = "https:" + resultItem.get("commidityUrl").toString() + "?a=a";

			tempTask.put("link", route);
			tempTask.put("rawlink", route);
			tempTask.put("linktype", "eccontent");

			reMap.put(Constants.ITEMLINK, tempTask);
			reMap.put(Constants.ITEMNAME, name);
			itemList.add(reMap);
			taskList.add(tempTask);
		}
		parsedata.put("items", itemList); // parseResult body
		String tempUrl = "https://csearch.suning.com/emall/brandquery/brandstoreQuery.jsonp?btc=30001790&keyword=&cp=pageNo&ps=48&st=&cityid=755&filters=&pcode=&callback=jsonpQueryByKeyword";
		int totalGoodsCount = (Integer) jsonMap.get("totalGoodsCount");//总商品数
		String pageno = getCresult(url, "&cp=(\\d+)");
		int pageNo = Integer.parseInt(pageno);
		if (totalGoodsCount - (pageNo + 1) * 48 > 0) {
			String nextPage = tempUrl.replaceAll("pageNo", (pageNo + 1) + "");
			Map<String, Object> nextpageTask = new HashMap<String, Object>();
			nextpageTask.put("link", nextPage);
			nextpageTask.put("rawlink", nextPage);
			nextpageTask.put("linktype", "eclist");
			taskList.add(nextpageTask);
			parsedata.put("nextpage", nextpageTask);
			parsedata.put("tasks", taskList);
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
}
