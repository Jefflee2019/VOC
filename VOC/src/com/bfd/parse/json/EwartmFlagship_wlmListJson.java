package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * 站点名：(京东)沃尔玛官方旗舰店
 *
 * cid:EwartmFlagship_wlm
 * 
 * 主要功能： 获取价格及处理翻页
 * note:**动态获取价格方式，没有下一页规则。更换成预处理+静态获取
 * 
 * @author lth
 *
 */
public class EwartmFlagship_wlmListJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(EwartmFlagship_wlmListJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient urlnormalizerClients, ParseUnit unit) {
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
				json = new String(data.getData(), "UTF-8");

				// 将ajax数据转化为json数据格式
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0 && (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["), json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
				}
				// 执行从json数据中提取自己感兴趣的数据
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
				// e.printStackTrace();
				// LOG.warn("json:" + json + ".url:" + taskdata.get("url"));
				LOG.warn("AMJsonParse exception,taskdat url=" + taskdata.get("url") + ".jsonUrl:" + data.getUrl(), e);
			}
		}

		// 组装返回结果
		JsonParserResult result = new JsonParserResult();
		try {
			result.setParsecode(parsecode);
			result.setData(parsedata);
		} catch (Exception e) {
			// e.printStackTrace();
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata, String json, String url, ParseUnit unit) {
		try {
			Object obj = JsonUtil.parseObject(json);
			if (obj instanceof Map) {
				Map<String, Object> dataMap = (Map<String, Object>) obj;
				if (dataMap.containsKey("data")) {
					List<Map<String, Object>> dataList = (List<Map<String, Object>>) dataMap.get("data");
					if (dataList != null && !dataList.isEmpty()) {
						List<Map<String,Object>> items = new ArrayList<Map<String,Object>>();
						List<Map<String,Object>> tasks = new ArrayList<Map<String,Object>>();
						for (Map<String, Object> itemTemp : dataList) {
							Map<String,Object> itemMap = new HashMap<String,Object>();
							Map<String,Object> itemLink = new HashMap<String,Object>();
							Map<String,Object> taskMap = new HashMap<String,Object>();
							// 价格
							if (itemTemp.containsKey("jp")) {
								String itemprice = itemTemp.get("jp").toString();
								itemMap.put("itemprice", itemprice);
							}
							// 商品名称
							if (itemTemp.containsKey("t")) {
								String itemname = itemTemp.get("t").toString();
								itemMap.put("itemname", itemname);
							}
							if (itemTemp.containsKey("sku")) {
								StringBuffer sb = new StringBuffer();
								// 拼接url
								String linkUrl = sb.append("https://item.jd.com/")
										.append(itemTemp.get("sku").toString()).append(".html?w=w").toString();
								// 拼接item元素
								itemLink.put("link", linkUrl);
								itemLink.put("rawlink", linkUrl);
								itemLink.put("linktype", "wlmEcContent");
								itemMap.put("itemlink", itemLink);

								// 拼接task元素
								taskMap.put("link", linkUrl);
								taskMap.put("rawlink", linkUrl);
								taskMap.put("linktype", "wlmEcContent");
							}
							items.add(itemMap);
							tasks.add(taskMap);
						}
						parsedata.put(Constants.ITEMS, items);
						parsedata.put(Constants.TASKS, tasks);
					}
				}
			}
		} catch (Exception e) {
			LOG.error("json parse error or json is null");
		}

	}
}
