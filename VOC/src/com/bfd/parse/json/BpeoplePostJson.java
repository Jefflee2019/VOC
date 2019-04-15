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
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.TextUtil;
/**
 * 站点名：人民网论坛
 * <p>
 * 主要功能：取到发贴内容
 * @author bfd_01
 *
 */
public class BpeoplePostJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(BpeoplePostJson.class);
	
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
//			LOG.info("url:" + data.getUrl() + ".json is " + json);
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

				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
//				e.printStackTrace();
//				LOG.warn("json :" + json + ".url:" + taskdata.get("url"));
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
//			e.printStackTrace();
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	
	}
	
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		try {
			// 处理数据，转换为json数据格式
			// 去掉<>标签,取得正文内容
			String contents = json.replaceAll("<.*?>", "");
			// contents = "{\"contents\":\"" + contents + "\"}";
			parsedata.put("contents", contents);
			// 取出图片的URL
			Pattern p = Pattern.compile("src=\".*?\"");
			Matcher m = p.matcher(json);
			List<String> imgList = new ArrayList<String>();
			while (m.find()) {
				String group = m.group();
				StringBuffer sb = new StringBuffer();
				sb.append(group.split("src=\"")[1]);
				imgList.add(sb.substring(0, sb.length() - 1).toString());
			}
			parsedata.put("contentimgs", imgList);
		} catch (Exception e) {
//			e.printStackTrace();
			LOG.error(e);
		}
	}
}
