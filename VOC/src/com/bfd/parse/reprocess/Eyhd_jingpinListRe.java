package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：Eyhd_hw
 * 
 * 主要功能：拼接添加下一页链接
 * 
 * @author bfd_03
 *
 */
public class Eyhd_jingpinListRe implements ReProcessor {

	private static final Pattern APPID_PATTERN = Pattern.compile("appId: (\\d+)");
	private static final Pattern PAGEINSTANCEID_PATTERN = Pattern.compile("value=\"(\\d+)\" id=\"pageInstance_id");
	private static final Pattern INSTANCEID = Pattern.compile("instanceId: (\\d+)");
	private static final Pattern MODULEPROTOTYPEID = Pattern.compile("modulePrototypeId: (\\d+)");
	private static final Pattern MODULETEMPLATEID = Pattern.compile("moduleTemplateId: (\\d+)");
	private static final Pattern PAGENO_PATTERN = Pattern.compile("pageNo=(\\d+)");
	
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();

		getNextpageUrl(unit, result);
		//修改商品页链接，避开消重
		Map<String, Object> resultData = result.getParsedata().getData();
		List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
//		for (Map<String, Object> map : tasks) {
//			String link = (String) map.get("link");
//			link = link + "?b=b";
//			map.put("link", link);
//			map.put("rawlink", link);
//		}
		return new ReProcessResult(processcode, processdata);
	}

	@SuppressWarnings("unchecked")
	private void getNextpageUrl(ParseUnit unit, ParseResult result) {
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = (String) unit.getTaskdata().get("url"); // 获取当前页面URL
		String nextpage = null;
		String data = unit.getPageData();
		int pageNo = 0;
		// http://module-jshop.jd.com/module/allGoods/goods.html?sortType=0&appId=756460&pageInstanceId=81612306
		// &pageNo=2&direction=1&instanceId=81612313&modulePrototypeId=55555&moduleTemplateId=905542&
		// refer=http://mall.yhd.com/index-1000000904.html
		String appid = null;
		String pageInstanceId = null;
		String instanceId = null;
		String modulePrototypeId = null;
		String moduleTemplateId = null;
		if (!url.contains("module-jshop.jd.com")) {
			appid = getParams(data,APPID_PATTERN);
			pageInstanceId= getParams(data,PAGEINSTANCEID_PATTERN);
			instanceId = getParams(data,INSTANCEID);
			modulePrototypeId = getParams(data,MODULEPROTOTYPEID);
			moduleTemplateId = getParams(data,MODULETEMPLATEID);
			nextpage = "http://module-jshop.jd.com/module/allGoods/goods.html?sortType=0&appId="
					+ appid + "&pageInstanceId=" + pageInstanceId + "&pageNo=2&direction=1&instanceId="
					+ instanceId + "&modulePrototypeId=" + modulePrototypeId + "&moduleTemplateId="
					+ moduleTemplateId + "&refer=" + url;
		} else {
			pageNo = Integer.valueOf(getParams(url,PAGENO_PATTERN));
			nextpage = url.replace("pageNo=" + pageNo, "pageNo=" + (pageNo+1));
		}
		
		Map<String, Object> commentTask = new HashMap<String, Object>();
		commentTask.put("link", nextpage);
		commentTask.put("rawlink", nextpage);
		commentTask.put("linktype", "eclist");
		if (nextpage != null && resultData != null && !resultData.isEmpty()) {
			resultData.put("nextpage", nextpage);
			List<Map> tasks = (List<Map>) resultData.get("tasks");
			tasks.add(commentTask);
		}
		ParseUtils.getIid(unit, result);
	}

	private String getParams(String data ,Pattern p) {
		String result = null;
		Matcher m = p.matcher(data);
		while (m.find()) {
			result = m.group(1);
		}
		return result;
	}
}
