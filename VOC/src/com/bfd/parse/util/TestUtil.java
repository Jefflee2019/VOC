package com.bfd.parse.util;

import java.util.HashMap;
import java.util.Map;

public class TestUtil {
//初始化taskdata数据结构，包含任务的各种配置
	public static Map<String, Object> initTaskMap(
			Map<String, Object> spiderData, String url, String iid, String cid,
			String type,int pagetypeid,int siteid,String pageType) {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> taskData = new HashMap<String, Object>();
		taskData.put("url", url);
		taskData.put("type", type);
		taskData.put("iid", iid);
		taskData.put("purl", "");
		taskData.put("datatype", "html");
		taskData.put("ajaxdatatype", "1");
		taskData.put("projname", "HuaweiVoc");
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
