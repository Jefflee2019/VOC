package com.bfd.parse.util;

import java.util.Date;
import java.util.Map;

import com.bfd.parse.ParseTestForPlugin;
import com.bfd.parse.client.DownloadClient;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.json.JsonParser;
import com.bfd.parse.preprocess.PreProcessor;
import com.bfd.parse.reprocess.ReProcessor;

/**
 * 插件测试类公共方法抽取
 * @author Chengjinquan
 *
 */
public class ParserTestUtils {
	@SuppressWarnings("unchecked")
	public static Map<String, Object> commonTest(String url, String cid, String pageType, 
			String siteid, int pageTypeid) throws Exception{
		DownloadClient crawler = new DownloadClient();
		// 以下为测试ajaxdata。
		String stringZip = crawler.getPage(url, "1", cid, "test", "", "",
				pageType, siteid);
		Map<String, Object> resMap = null;
		resMap = (Map<String, Object>) JsonUtil.parseObject(stringZip);
		Map<String, Object> spiderData = (Map<String, Object>) resMap
				.get("spiderdata");
		return JsonTestUtil.initTaskMap(spiderData, url, "", cid, "",
				pageTypeid, Integer.parseInt(siteid), pageType);
	}
	
	public static void jsonTest(String url, String cid, String pageType, 
			String siteid, int pageTypeid, JsonParser parser) throws Exception{
		new ParseTestForPlugin().parseTest(getUnit(url, cid, pageType, siteid, pageTypeid), null, parser, null);
	}
	
	public static void reprocessTest(String url, String cid, String pageType, 
			String siteid, int pageTypeid,ReProcessor process) throws Exception{
		new ParseTestForPlugin().parseTest(getUnit(url, cid, pageType, siteid, pageTypeid), null, null, process);
	}
	
	public static void preprocesTest(String url, String cid, String pageType, 
			String siteid, int pageTypeid, PreProcessor pre) throws Exception{
		new ParseTestForPlugin().parseTest(getUnit(url, cid, pageType, siteid, pageTypeid), pre, null, null);
	}
	
	private static ParseUnit getUnit(String url, String cid, String pageType, 
			String siteid, int pageTypeid) throws Exception{
		Map<String, Object> map = commonTest(url, cid, pageType, siteid, pageTypeid);
		return ParseUnit.fromMap(map, new Date().getTime());
	}
}
