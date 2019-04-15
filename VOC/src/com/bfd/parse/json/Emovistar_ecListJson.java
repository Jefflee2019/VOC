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
 * 站点名：头条 
 * <p>
 * 主要功能： 抓取指定链接的商品
 * <p>
 * @author bfd_01
 *
 */
public class Emovistar_ecListJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(Emovistar_ecListJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient urlnormalizerClients,
			ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();

		// 遍历dataList
		for (Object obj : dataList) {
			JsonData data = (JsonData) obj;
			// 判断该ajax数据是否下载成功
			if (!data.downloadSuccess()) {
				continue;
			}
			// 解压缩ajax数据
			String json = TextUtil.getUnzipJson(data, unit);
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
				// 执行从json数据中提取自己感兴趣的数据
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
				//e.printStackTrace();
				//LOG.warn("json:" + json + ".url:" + taskdata.get("url"));
				LOG.warn(
						"AMJsonParse exception,taskdat url="
								+ taskdata.get("url") + ".jsonUrl:"
								+ data.getUrl(), e);
			}
		}

		// 组装返回结果
		JsonParserResult result = new JsonParserResult();
		try {
			result.setParsecode(parsecode);
			result.setData(parsedata);
		} catch (Exception e) {
			//e.printStackTrace();
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		Map<String, Object> jsonMap = null;
		try {
			jsonMap = (Map<String, Object>) JsonUtils.parseObject(json);
		} catch (Exception e) {
			// e.printStackTrace();
			LOG.error("executeParse error " + e.getCause().getMessage());
		}
		Map portletData = (Map) jsonMap.get("portletData");
		Map data = (Map) portletData.get("data");
		List<Map<String, Object>> devices = (List<Map<String, Object>>) data.get("devices");
		Boolean isLastPage = (Boolean) data.get("isLastPage");
		int currentPage = (int) data.get("currentPage");
		List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
		parsedata.put("tasks", taskList);
		List<Map<String, Object>> itemList = new ArrayList<Map<String, Object>>();

		for (Map<String, Object> resultItem : devices) {
			Map<String, Object> reMap = new HashMap<String, Object>();
			Map<String, Object> tempTask = new HashMap<String, Object>();
			Map<String, Object> price = (Map<String, Object>) resultItem.get("price");
			String integer = price.get("integer").toString();
			String name = resultItem.get("name").toString();
			String route = "https://mimovistar.movistar.com.ec" + resultItem.get("url").toString();

			tempTask.put("link", route);
			tempTask.put("rawlink", route);
			tempTask.put("linktype", "eccontent");

			reMap.put(Constants.ITEMLINK, tempTask);
			reMap.put(Constants.ITEMNAME, name);
			reMap.put("itemprice", "$"+integer);
			itemList.add(reMap);
//			taskList.add(tempTask);
			
		}
		parsedata.put("items", itemList); // parseResult body
		
		if (!isLastPage) {
//				https://mimovistar.movistar.com.ec/web/guest/movil-pospago?p_p_id=phoneslist_
//				WAR_tfnecuportalwar&p_p_lifecycle=2&p_p_state=normal&p_p_mode=view&p_p_cacheability
//						=cacheLevelPage&p_p_col_id=column-2&p_p_col_pos=1&p_p_col_count=2&p_p_
//						resource_id=loadDevices&loadDevicesRequest=%7B%22filtering%22%3A%5B%5D%2C%22
//						page%22%3A1%2C%22category%22%3A%22Postpaid+equipment%22%2C%22isSearch%22%3Afalse%2C%22planCode%22%3A%22%22%7D
			String nextPage = url.replaceAll("page%22%3A" + currentPage, "page%22%3A" + (currentPage + 1));
			Map<String, Object> nextpageTask = new HashMap<String, Object>();
			nextpageTask.put("link", nextPage);
			nextpageTask.put("rawlink", nextPage);
			nextpageTask.put("linktype", "eclist");
			taskList.add(nextpageTask);
			parsedata.put("nextpage", nextpageTask);
			parsedata.put("tasks", taskList);
		}
	
	}

}
