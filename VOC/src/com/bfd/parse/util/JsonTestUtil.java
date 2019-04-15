package com.bfd.parse.util;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.json.JsonData;
import com.bfd.parse.util.TextUtil;

public class JsonTestUtil {
	
	public static String json_data;
	/**
	 * 格式化json数据，去掉前面或者后面的无用数据
	 * @param json
	 * @return
	 */
	public static String jsonformat(String json) {
		// 将ajax数据转化为劲松数据格式
		if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
				&& (json.indexOf("[") < json.indexOf("{"))) {
			json = json.substring(json.indexOf("["), json.lastIndexOf("]") + 1);
		} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
			json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
		}
		return json;
	}
	
	/**
	 * 初始化taskdata数据结构，包含任务的各种配置
	 * @param spiderData
	 * @param url
	 * @param iid
	 * @param cid
	 * @param type
	 * @return
	 */
//	public static Map<String, Object> initTaskMap(
//			Map<String, Object> spiderData, String url, String iid, String cid,
//			String type) {
//		Map<String, Object> map = new HashMap<String, Object>();
//		Map<String, Object> taskData = new HashMap<String, Object>();
//		taskData.put("url", url);
//		taskData.put("type", type);
//		taskData.put("iid", iid);
//		taskData.put("purl", "");
//		taskData.put("datatype", "html");
//		taskData.put("ajaxdatatype", "1");
//		taskData.put("projname", "ItemMonitor");
//		taskData.put("cate", "cate");
//		taskData.put("parsetype", 0);
//		taskData.put("cid", cid);
//
//		map.put("taskdata", taskData);
//		map.put("spiderdata", spiderData);
//
//		return map;
//
//	}
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
		taskData.put("datatype", "json");
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
	
	/**
	 * 在控制台打印出json数据
	 * @param unit
	 */
	public static void testAjaxData(ParseUnit unit) {
		List<JsonData> jsonDatas = TextUtil.wrapJsonData(unit, true);
		// //如果转字符串出错，就用主页自己的charset。
		String defaultCharset = "GBK";
		unit.setPageEncode(defaultCharset);
		int i = 0;
		for (JsonData tm : jsonDatas) {
			i++;
			System.out.println("----------------------" + i + ".charset:"
					+ tm.getCharset());
			try {
				System.out.println("url:" + tm.getUrl() + ".\n" +"\n"
						+ new String(tm.getData(), tm.getCharset()));
				json_data = new String(tm.getData(), tm.getCharset());
			} catch (Exception e) {
				try {
					System.out.println("url:" + tm.getUrl() + "."
							+ new String(tm.getData(), defaultCharset));
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}
	}
	/**
	 * 打印map中的键值对
	 * @param map
	 */
	public static void test(Map<String,Object> map) {
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			System.out.println("key = " + entry.getKey() + " and value = "
					+ entry.getValue());
		}
	}
}
