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
public class Eclaro_chileListJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(Eclaro_chileListJson.class);

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
	
	public static void main(String[] args) {
		Eclaro_chileListJson chileListJson = new Eclaro_chileListJson();
		String json = "";
		chileListJson.executeParse(null, json, "url", null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		try {
			List<Map<String, Object>> data = (List<Map<String, Object>>) JsonUtils.parseArray(json);
			List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
			parsedata.put("tasks", taskList);
			List<Map<String, Object>> itemList = new ArrayList<Map<String, Object>>();
			for (Map<String, Object> resultItem : data) {
				Map<String, Object> reMap = new HashMap<String, Object>();
				Map<String, Object> tempTask = new HashMap<String, Object>();
				String precio_prepago = resultItem.get("precio_prepago").toString();
				String marca = resultItem.get("marca").toString();
				String modelo_comercial = resultItem.get("modelo_comercial").toString();
				String route = "https://equipos.clarochile.cl/detalle.html?id=" + resultItem.get("id").toString();

				tempTask.put("link", route);
				tempTask.put("rawlink", route);
				tempTask.put("linktype", "eccontent");

				reMap.put(Constants.ITEMLINK, tempTask);
				reMap.put(Constants.ITEMNAME, marca + " " + modelo_comercial);
				reMap.put("itemprice", "$"+precio_prepago);
				itemList.add(reMap);
				taskList.add(tempTask);
				
			}
			parsedata.put("items", itemList); // parseResult body
			
		} catch (Exception e) {
			// e.printStackTrace();
			LOG.error("executeParse error " + e.getMessage());
		}
	}

}
