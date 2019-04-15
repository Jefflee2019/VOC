package com.bfd.parse;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.kafka7.KfkProducer;
import com.bfd.crawler.utils.ConfigUtils;
import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.facade.parseunit.ParseUnit;

public class DataSaver {

	private static final Log LOG = LogFactory.getLog(DataSaver.class);

	private String workName;
//	private DataOperatorClient dataOperatorClient;
	private static final Boolean saveSource = ConfigUtils.getInstance().getBoolProp("SaveSource", true);

	public DataSaver(String name) {
		this.workName = name;
//		dataOperatorClient = new DataOperatorClient();
	}

	public void saveData(ParseUnit unit, ParseResult result) {
		if (unit.needSave()) {
//			if (result.getParsedata().getParsecode() == ParseResult.SUCCESS
//					|| result.getParsedata().getParsecode() == ParseResult.OFF) {
				//保存解析结果
				saveParseData(unit.getTaskdata(), result);
//			}
			
			//:TODO不需要保存页面源码了
//			if (saveSource && !checkResMd5(unit, result)) {
//				//保存页面源码
//				saveSourceData(unit.getData(), unit.getAjaxdata(), unit.getTaskdata());
//			}
		}
	}

	/**
	 * md5相同返回true
	 * 
	 * @param unit
	 * @param result
	 */
//	private boolean checkResMd5(ParseUnit unit, ParseResult result) {
//		Map<String, Object> data = result.getParsedata().getData();
//		Map<String, Object> tmp = new HashMap<String, Object>();
//		String attrMd5 = null;
//		for (Entry<String, Object> entry : data.entrySet()) {
//			Object value = entry.getValue();
//			if (entry.getKey().equals("attr")) {
//				if (value != null && value instanceof Map) {
//					attrMd5 = (String) ((Map) value).get("resmd5");
//					((Map) value).remove("resmd5");
//				}
//				continue;
//			}
//			tmp.put(entry.getKey(), value);
//		}
//		// 计算解析结果的resmd5并与attribute中的做比较。
//		String dataMd5 = DataUtil.calcMD5(JsonUtil.toJSONString(tmp));
//		LOG.info(workName + " got attrmd5=" + attrMd5 + ", datamd5=" + dataMd5 + ", url=" + unit.getUrl());
//		if (dataMd5.equals(attrMd5)) {
//			LOG.info(workName + " got datamd5 is same as resmd5, url=" + unit.getUrl());
//			return true;
//		}
//		data.put("resmd5", dataMd5);
//		return false;
//	}

	/**
	 * 保存源文件数据
	 * 
	 * 若存在ajaxdata, 则将ajaxdata拼接到data后边，同时添加jsonlength字段
	 * 
	 * @param data
	 * @param ajaxdata
	 * @param taskdata
	 */
//	public void saveSourceData(final String data, List<Map<String, Object>> ajaxdata, Map<String, Object> taskdata) {
//		try {
//			Map<String, Object> reqMap = new HashMap<String, Object>();
//			reqMap.putAll(taskdata);
//			String reqData = "";
//			if (StringUtils.isNotEmpty(data)) {
//				reqData = data;
//			}
//			if (ajaxdata != null && ajaxdata.size() > 0) {
//				String jsonAjaxdata = JsonUtil.toJSONString(ajaxdata);
//				reqMap.put("jsonlength", jsonAjaxdata.getBytes().length);
//				reqData += jsonAjaxdata;
//			}
//			if (StringUtils.isNotEmpty(reqData)) {
//				LOG.info(workName + " saving source data...");
//				int status = dataOperatorClient.saveData(JsonUtil.toJSONString(reqMap), reqData);
//				LOG.info(workName + " saved source data, status=" + status + ", url=" + taskdata.get("url"));
//			}
//		} catch (Exception e) {
//			LOG.warn(workName + " saving source data exception, url=" + taskdata.get("url"), e);
//		}
//	}

	/**
	 * 保存解析结果数据
	 * 
	 * @param taskData
	 * @param parseData
	 */
	public void saveParseData(Map<String, Object> taskData, ParseResult result) {
		int status = 0;

		LOG.info(workName + " saving result data...");

		//TODO:写解析结果到kafka,topic名称需要修改
		KfkProducer.getInstance().send(taskData.get("parsequeuetopic").toString(), JsonUtils.toJSONString(result));
//		status = dataOperatorClient.saveData(JsonUtil.toJSONString(req), JsonUtil.toJSONString(parseData));
		LOG.info(workName + " saved result data, status=" + status + ", url=" + taskData.get("url"));
	}

//	public DataOperatorClient getDataOperatorClient() {
//		return dataOperatorClient;
//	}
}
