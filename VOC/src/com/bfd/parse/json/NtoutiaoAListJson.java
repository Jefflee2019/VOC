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
import com.bfd.parse.util.TextUtil;

/**
 * 站点名：头条 
 * <p>
 * 主要功能： 抓取指定链接的商品
 * <p>
 * @author bfd_01
 *
 */
public class NtoutiaoAListJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(NtoutiaoAListJson.class);

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
//		String[] link = json.split(System.getProperty("line.separator"));
		String[] link = json.split("\n");
		List list = new ArrayList();
		List tasklist = new ArrayList();
		if (link.length > 0) {
			for (int i=0;i <link.length; i++) {
				Map item = new HashMap();
				Map task = new HashMap();
				Map temp = new HashMap();
				temp.put("link", link[i]);
				temp.put("rawlink", link[i]);
				temp.put("linktype", "newscontent");
				item.put("itemlink", temp);
				item.put("title", i);
				list.add(item);
				
				task.put("link", link[i]);
				task.put("rawlink", link[i]);
				task.put("linktype", "newscontent");
				tasklist.add(task);
			}
			parsedata.put(Constants.ITEMS, list);
			parsedata.put(Constants.TASKS, tasklist);
		}
	}

}
