package com.bfd.parse.test.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.utils.DataUtil;

public class CreateParseUnit {

	/**
	 * 生成解析使用的map
	 * 
	 * @param projectname
	 * @param cid
	 * @param siteid
	 * @param pagetypeid
	 * @param url
	 * @param charset
	 * @param taskOtherData
	 * @param ajaxDataUrl
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> createMap(String projectname, String cid, int siteid, int pagetypeid, String url, String charset,
			Map<String, Object> taskOtherData, List<String> ajaxDataUrl) throws Exception {

		HttpUtil contentHttpUtil = new HttpUtil(url, charset, "", null, "", null, true);
		List<HttpUtil> ajaxHttpUtil = new ArrayList<HttpUtil>();
		if (ajaxDataUrl != null && ajaxDataUrl.size() > 0) {
			for (String ajaxUrl : ajaxDataUrl) {
				HttpUtil ajax = new HttpUtil(ajaxUrl, charset, url, null, "", null, true);
				ajaxHttpUtil.add(ajax);
			}
		}
		return createMap(projectname, cid, siteid, pagetypeid, url, contentHttpUtil, charset, taskOtherData, ajaxHttpUtil);
	}

	/**
	 * 生成解析使用的map
	 * 
	 * @param projectname
	 * @param cid
	 * @param siteid
	 * @param pagetypeid
	 * @param url
	 * @param charset
	 * @param taskOtherData
	 * @param ajaxDataUrl
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> createMap2(String projectname, String cid, int siteid, int pagetypeid, String url, String charset,
			Map<String, Object> taskOtherData, List<HttpUtil> ajaxDataUrl) throws Exception {
		HttpUtil contentHttpUtil = new HttpUtil(url, charset, "", null, "", null, true);
		if (ajaxDataUrl != null) {
			for (HttpUtil h : ajaxDataUrl) {
				h.setReferer(url);
			}
		}
		return createMap(projectname, cid, siteid, pagetypeid, url, contentHttpUtil, charset, taskOtherData, ajaxDataUrl);
	}

	/**
	 * 生成解析使用的map
	 * 
	 * @param projectname
	 * @param cid
	 * @param siteid
	 * @param pagetypeid
	 * @param url
	 * @param contentHttpUtil
	 * @param charset
	 * @param taskOtherData
	 * @param ajaxDataUrl
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> createMap(String projectname, String cid, int siteid, int pagetypeid, String url,
			HttpUtil contentHttpUtil, String charset, Map<String, Object> taskOtherData, List<HttpUtil> ajaxDataUrl) throws Exception {
		DownloaderParam data = new DownloaderParam(url, contentHttpUtil.excute(), charset);

		List<DownloaderParam> ajaxData = new ArrayList<DownloaderParam>();
		if (ajaxDataUrl != null && ajaxDataUrl.size() > 0) {
			for (HttpUtil ajaxUrl : ajaxDataUrl) {
				String ajaxStr = ajaxUrl.excute();
				DownloaderParam ajax = new DownloaderParam(ajaxUrl.getUrl(), ajaxStr, ajaxUrl.getCharset());
				ajaxData.add(ajax);
			}
		}
		return createTaskMap(projectname, cid, siteid, pagetypeid, url, taskOtherData, createSpiderData(data, ajaxData));
	}

	/**
	 * 生成下载结果
	 * 
	 * @param item
	 * @param ajaxData
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> createSpiderData(DownloaderParam item, List<DownloaderParam> ajaxData) throws Exception {
		Map<String, Object> spiderdata = new HashMap<String, Object>();
		spiderdata.put("code", 0);
		spiderdata.put("charset", item.getCharset());
		spiderdata.put("data", item.getData());
		spiderdata.put("length", spiderdata.get("data").toString().length());
		List<Map<String, Object>> jsonData = new ArrayList<Map<String, Object>>();
		if (ajaxData != null && ajaxData.size() > 0) {
			for (DownloaderParam ajax : ajaxData) {
				Map<String, Object> ajaxMap = new HashMap<String, Object>();
				ajaxMap.put("url", ajax.getUrl());
				ajaxMap.put("data", DataUtil.zipAndEncode(ajax.getData(),"utf-8"));
				ajaxMap.put("charset", ajax.getCharset());
				ajaxMap.put("code", 0);
				jsonData.add(ajaxMap);
			}
		}
		spiderdata.put("ajaxdata", jsonData);
		return spiderdata;
	}

	public static Map<String, Object> createTaskMap(String projectname, String cid, int siteid, int pagetypeid, String url,
			Map<String, Object> taskOtherData, Map<String, Object> spiderdata) {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> taskdata = new HashMap<String, Object>();
		taskdata.put("cid", cid);
		taskdata.put("siteid", siteid);
		taskdata.put("url", url);
		taskdata.put("pagetypeid", pagetypeid);
		taskdata.put("traceflag", 0);
		taskdata.put("datatype", "html");
		taskdata.put("projectname", projectname);
		taskdata.put("iid", DataUtil.calcMD5(url));
		if (taskOtherData != null) {
			taskdata.putAll(taskOtherData);
		}
		map.put("taskdata", taskdata);
		map.put("spiderdata", spiderdata);
		return map;
	}
}
