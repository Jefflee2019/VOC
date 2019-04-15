package com.bfd.parse.preprocess;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 站点名：央广网(新闻) 
 * 
 * 主要功能: 通过预处理插件手工发送http请求获取系统无法下载的页面。
 * 
 * @author bfd_03
 *
 */
public class NcnrContentPre implements PreProcessor {
	private static HttpURLConnection conn;
	public static final String USER_AGENT = "User-Agent";
	public static final String ACCEPT = "Accept";
	public static final String ACCEPT_LANGUAGE = "Accept-Language";
	public static final String COOKIE = "Cookie";
	public static final String REFERER = "Referer";

	@Override
	public boolean process(ParseUnit unit, ParserFace face) {
		String url = unit.getUrl();
		if(!url.contains("native")){
			return true;
		}
		String pageData = parsePageData(unit);
		if(!pageData.isEmpty()){
			unit.setPageData(pageData);
		}
		try {
			unit.setPageBytes(pageData.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {

		}
		unit.setPageEncode("utf8");
		return true;
	}

	/**
	 * @param pageData
	 * @return
	 */
	public String parsePageData(ParseUnit unit) {
		String rtStr = "";
		String url = unit.getUrl(); 

		Map<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("url", url);
		paraMap.put(USER_AGENT, "Mozilla/5.0");
		paraMap.put(ACCEPT,"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		paraMap.put(ACCEPT_LANGUAGE, "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		paraMap.put(REFERER, url);
		
		try {
			rtStr = getPageData(paraMap, "GB2312");
		} catch (Exception e) {
			
		}
		return rtStr;
	}
		
	   public static String getPageData(Map<String, String> paraMap, String codeType)
				throws Exception {
			String rtPageData = "";
			if (!paraMap.containsKey("url")) {
				return rtPageData;
			}
			String url = paraMap.get("url");
			StringBuffer response = new StringBuffer();

			URL urlObj = new URL(url);
			conn = (HttpURLConnection) urlObj.openConnection();
			conn.setRequestMethod("GET");
			conn.setUseCaches(false);

			Set<String> keySet = paraMap.keySet();
			for (String key : keySet) {
				conn.setRequestProperty(key, paraMap.get(key));
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(
					conn.getInputStream(), codeType));

			String inputLine = "";
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			rtPageData = response.toString();
			return rtPageData;
		}
	
}
