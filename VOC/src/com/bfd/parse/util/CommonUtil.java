package com.bfd.parse.util;

import java.util.HashMap;
import java.util.Map;

import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.json.JsonData;

public class CommonUtil {
	public static String parserComm(JsonData data, ParseUnit unit){
		String jsonStr = TextUtil.getUnzipJson(data, unit);
			//转化为劲松数据公式
		if (jsonStr.indexOf("[") >= 0 && jsonStr.indexOf("]") >= 0
				&& (jsonStr.indexOf("[") < jsonStr.indexOf("{"))) {
			jsonStr = jsonStr.substring(jsonStr.indexOf("["),
					jsonStr.indexOf("]") + 1);
		}
		else if (jsonStr.indexOf("{") >= 0 && jsonStr.indexOf("}") >= 0) {
			jsonStr = jsonStr.substring(jsonStr.indexOf("{"),
					jsonStr.lastIndexOf("}") + 1);
		}
		return jsonStr;
	}

	public static Map<String, Object> initTaskMap(Map<String, Object> spiderData, String url,
			String iid, String cid, String type, int siteid, int pagetypeid, String pageType) {
		//把数据放到新的map中返回
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> taskData = new HashMap<String, Object>();
		taskData.put("url", url);
		taskData.put("type", type);
		taskData.put("iid", iid);
		taskData.put("purl", "");
		taskData.put("datatype", "html");
		taskData.put("ajaxdatatype", "1");
		taskData.put("projname", "ItemMonitor");
		taskData.put("cate", "cate");
		taskData.put("parsetype", 0);
		taskData.put("cid", cid);
		taskData.put("pagetypeid", pagetypeid);
		taskData.put("siteid", siteid);
		taskData.put("pagetype", pageType);

		map.put("taskdata", taskData);
		map.put("spiderdata", spiderData);
		
		return map;
	}
}
