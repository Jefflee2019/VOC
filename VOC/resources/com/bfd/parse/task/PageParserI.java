package com.bfd.parse.task;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Ice.Current;

import com.bfd.crawler._PageParserDisp;
import com.bfd.crawler.utils.DataUtil;
import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.Constants;
import com.bfd.parse.config.ConfigLoader;
import com.bfd.parse.facade.IParse;
import com.bfd.parse.facade.ParserFactory;
import com.bfd.parse.file.DataLoader;
import com.bfd.parse.test.ParseTester;
import com.bfd.parse.test.TestRequest;
import com.bfd.parse.test.TestResponse;
import com.bfd.parse.test.Tester;
import com.bfd.parse.test.TesterFactory;
import com.bfd.parse.threadmanager.ParseWorker;
import com.bfd.parse.util.JsonUtil;

public class PageParserI extends _PageParserDisp {

	private static final Log log = LogFactory.getLog(PageParserI.class);

	@Override
	public String parse(String request, Current __current) {
		log.info("request:"+request);
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		result.put(Constants.code, ParseWorker.addJob(request, ParseJob.TYPE_DATA) ? 0 : 1);
		return JsonUtil.toJSONString(result);
	}

	@Override
	public String parseFile(String request, Current __current) {
//		log.info("ParseFile receive a request : " + request);
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		// result.put("code", ParseWorker.addJob(request, ParseJob.TYPE_FILE) ?
		// 0 : 1);
		result.put(Constants.code, DataLoader.loadFileData(request, "") ? 0 : 1);
		return JsonUtil.toJSONString(result);
	}


	private String parseWeibo(Map pageData,Map reqMap){
		Map params = new HashMap();
		params.put("pageData", pageData);
		params.put("reqMap", reqMap);
		Object rs = ParserFactory.getParser(IParse.parser_js).parse(JsonUtils.toJSONString(params));
		return JsonUtils.toJSONString(rs);
	}
	@Override
	public String parseTest(String page, String param, Current __current) {
		log.info("parseTest.param:"+param);
		//微博解析调用的接口
		try {
			Map<String, Object> reqMap = (Map<String, Object>) JsonUtil.parseObject(param);
			if(reqMap.containsKey("type")&&reqMap.get("type").toString().equals("weibo")){
				Map<String, Object> pageMap = (Map<String, Object>) JsonUtil.parseObject(page);
				String rs = parseWeibo(pageMap,reqMap);
				return rs;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			return "{\"code\":2,\"desc\":\"params is not json\"}";
		}
		TestRequest request = TestRequest.create(page, param);

		//以下代码块为请求参数里面有testType
		if (request != null) {
			//request.getTesterName()取值为param中的testType，比如：DomTmpl，DomPath
			//页面的左侧模板测试和页面的右侧模板测试testType都为DomTmpl，
			
			Tester tester = TesterFactory.getTester(request.getTesterName());
			log.info(request.getUrl()+" request:"+request.getTesterName()+",className:"+tester.getClass().getName());
			if (tester != null) {
				return tester.test(request).toJsonString();
			}
			return TestResponse.create().toJsonString();
		}

		String res = "";
		try {
			if (StringUtils.isNotEmpty(page) && StringUtils.isNotEmpty(param)) {
				@SuppressWarnings("unchecked")
				Map<String, Object> paramMap = (Map<String, Object>) JsonUtil.parseObject(param);
				res = new ParseTester().test(DataUtil.unzipAndDecode(page), paramMap);
				//补全操作不需要编码为gbk。
				if(((String)paramMap.get("type")).equalsIgnoreCase("tagbalance")){
					return res;
				}
				res = new String(res.getBytes("gbk"), "gbk");
				return res;
			}
		} catch (Exception e) {
			log.info("Parse test Err=", e);
		}
		return "{\"code\":1}";
	}

//	@Override
//	public void reloadConfig(String reqData, Current __current) {
//		try {
//			Object request = JsonUtil.parseObject(reqData);
//			if (request instanceof Map) {
//				if (ConfigLoader.load((Map<String, Object>) request))
//					log.info("reloaded config success");
//				else
//					log.info("reloaded config failed");
//			}
//		} catch (Exception e) {
//			log.warn("reloadconfig request err:", e);
//		}
//	}
}
