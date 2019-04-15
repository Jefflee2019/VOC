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
 * @author bfd_01
 */
public class Ejollychic_saListJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(Ejollychic_saListJson.class);

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
		LOG.error("999999999999999"+json);
		Map<String, Object> jsonMap = null;
		try {
			jsonMap = (Map<String, Object>) JsonUtils.parseObject(json);
		} catch (Exception e) {
			// e.printStackTrace();
			LOG.error("executeParse error " + e.getCause().getMessage());
		}
		Map data = (Map) jsonMap.get("data");
		Map data1 = (Map) data.get("data");
		List<Map<String, Object>> edtionGoodsList = (List<Map<String, Object>>) data1.get("edtionGoodsList");
		Map pageInfo = (Map) data1.get("pageInfo");
		Integer isLastPage = (Integer) pageInfo.get("isLastPage");
		int pageNum = (int) pageInfo.get("pageNum");
		List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
		parsedata.put("tasks", taskList);
		List<Map<String, Object>> itemList = new ArrayList<Map<String, Object>>();

		for (Map<String, Object> resultItem : edtionGoodsList) {
			Map<String, Object> reMap = new HashMap<String, Object>();
			Map<String, Object> tempTask = new HashMap<String, Object>();
			String name = resultItem.get("goodsName").toString();
			String route = resultItem.get("goodsUrl").toString();
			String promotePrice = resultItem.get("promotePrice").toString();

			tempTask.put("link", route);
			tempTask.put("rawlink", route);
			tempTask.put("linktype", "eccontent");

			reMap.put(Constants.ITEMLINK, tempTask);
			reMap.put(Constants.ITEMNAME, name);
			reMap.put("itemprice", "$" + promotePrice);
			itemList.add(reMap);
//			taskList.add(tempTask);
		}
		parsedata.put("items", itemList); // parseResult body
		
		if (isLastPage == 0) {
//			https://www.jollychic.com/TopicNewAction/getAjaxEdtionGoodsList?params=%7B%22edtionId%22%3A134076%2C%22catId%22%3A356522%2C%22pageNum%22%3A1%2C%22localDepotSelected%22%3A0%2C%22pageSize%22%3A60%7D
			String nextPage = url.replaceAll("pageNum%22%3A" + pageNum, "pageNum%22%3A" + (pageNum + 1));
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
