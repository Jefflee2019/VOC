package com.bfd.parse.test.weibosinaparser;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.DownloadClient;
import com.bfd.parse.util.JsonUtil;


public class Utils {
	private static final Log LOG = LogFactory.getLog(AWeiBoParser.class);
	public static String getHtml(String json) throws Exception {
		Map<String, String> contentMap = null;
		String contentHtml = "";
//		try {
			contentMap = (Map) JsonUtil.parseObject(json);
			contentHtml = contentMap.get("html");
//		} catch (Exception e) {
////			e.printStackTrace();
//			LOG.error("gethtml error parse json error");
//			
//		}
		return contentHtml;
	}
	
	public static void getTask(String url){
		DownloadClient client = new DownloadClient();
		String stringZip = client.getPage(url, "1","sina","weibo", AWeiBoParser.refer, AWeiBoParser.cookie);
		Map<String, Object> resMap = null;
		try {
			resMap = (Map<String, Object>) JsonUtil.parseObject(stringZip);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		Map<String, Object> spider = (Map<String, Object>) resMap
				.get("spiderdata");
		Map<String, Object> taskdata = new HashMap<String, Object>();
		taskdata.put("url", url);
		Task task = new Task();
		task.setTaskdata(taskdata);
		task.setSpiderdata(spider);
		System.out.println("data:"+JsonUtil.toJSONString(task));
	}
	
	public static void addAttr(Map rs ,Task task){
		if(task.getTaskdata().containsKey(Constants.attr)){
			rs.putAll((Map)task.getTaskdata().get(Constants.attr));
		}
		if(task.getTaskdata().containsKey(Constants.cid)){
			rs.put(Constants.cid, task.getTaskdata().get(Constants.cid));
		}
		if(task.getTaskdata().containsKey(Constants.pagetype)){
			rs.put(Constants.pagetype, task.getTaskdata().get(Constants.pagetype));
		}
	}
	
	
	public static void main(String[] args) {
		String url = "http://weibo.com/aj/comment/big?ajwvr=6&id=3834861489595433&__rnd=1430210206178";
		getTask(url);
	}
}
