package com.bfd.parse.client;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Ice.ObjectPrx;

import com.bfd.crawler.TraceManagePrx;
import com.bfd.crawler.TraceManagePrxHelper;
import com.bfd.crawler.utils.ConfigUtils;
import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;

public class TraceTaskClient {
	private static final Log logger = LogFactory.getLog(TraceTaskClient.class);
	public static int maxTryCount = 3;
	
	public void reportData(String taskId,String json){
		int i=0;
		while(i<maxTryCount){
			try {
				logger.info("after reportdata");
				return;
			} catch (Exception e) {
				e.printStackTrace();
//				if(init()){
//					getService().reportTraceData(taskId, "parse", (System.currentTimeMillis()/1000)+"", json);
//				}
			}
		}
		
	}
	
	public void reportTraceTask(ParseUnit unit,String serviceName
					,String eventname,String enentdata,int eventcode){
		//如果不是染色任务，就不上报数据。
		if(unit.getTraceflag()!=1){
			return;
		}
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("siteid", unit.getSiteId());
		data.put("pagetypeid", unit.getPageTypeId());
		data.put("parenttaskid", 0);
		data.put("host", Constants.host);
		data.put("eventname", eventname);
		data.put("eventcode", eventcode);
		data.put("eventdata", enentdata);
		data.put("servicename", serviceName);
		logger.debug("url:"+unit.getUrl()+" report data :"+JsonUtils.toJSONString(data));
		reportData(unit.getTaskId(), JsonUtils.toJSONString(data));
	}
	
	public void reportTraceTask(Map<String,Object> unit, String serviceName, String eventname, String enentdata, int eventcode) {
		
		if (!unit.containsKey("traceflag") || !unit.containsKey(Constants.taskid)) {
			return;
		}
		int trace = Integer.parseInt(unit.get("traceflag").toString());
		// 如果不是染色任务，就不上报数据。
		if (trace != 1) {
			return;
		}
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("siteid", unit.get("siteid"));
		data.put("pagetypeid", 0);
		data.put("parenttaskid", 0);
		data.put("host", Constants.host);
		data.put("eventname", eventname);
		data.put("eventcode", eventcode);
		data.put("eventdata", enentdata);
		data.put("servicename", serviceName);
		logger.debug("url:" + unit.get("url") + " report data :" + JsonUtils.toJSONString(data));
		reportData(unit.get(Constants.taskid).toString(), JsonUtils.toJSONString(data));
	}
	
	
	public static void main(String[] args) {
		TraceTaskClient client = new TraceTaskClient();
		client.reportData("123", "json");
		
	}

}
