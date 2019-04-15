package com.bfd.parse.reprocess;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.entity.Constants;

/**
 * 站点名：Bsohu
 * 
 * 功能：加上回复数
 * 
 * @author bfd_06
 */
public class BsohuListRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(BsohuListRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		/**
		 * 加上系统默认回复数-1024
		 */
		if (resultData.containsKey(Constants.ITEMS)) {
			List<Map<String, Object>> items = (List<Map<String, Object>>) resultData
					.get(Constants.ITEMS);
			for (Map<String, Object> item : items) {
				item.put(Constants.REPLY_CNT, -1024);
			}
		}
		
		/*
		 * 得到转换后的链接
		 * 
		 * if (resultData != null && resultData.containsKey(Constants.TASKS)) {
		 * List<Map<String, Object>> tasksList = (List<Map<String, Object>>)
		 * resultData .get(Constants.TASKS); if (tasksList.size() != 0) { for
		 * (Map<String, Object> tempMap : tasksList) { if
		 * (tempMap.containsKey("link")) { String link =
		 * tempMap.get("link").toString(); String channelid =
		 * match("\"channelid\":\"(\\w+)\"", sendGet(link)); if (channelid !=
		 * null) { String realLink = link .replace("//s", "//" + channelid)
		 * .replace("?action=link&forumid=", "") .replace("&threadid=",
		 * "/thread/") .replace("&floorid=0", ""); realLink = realLink + "/p1";
		 * tempMap.put("link", realLink); tempMap.put("rawlink", realLink); }
		 * 
		 * } } } }
		 */
		
		return new ReProcessResult(processcode, processdata);
	}

	/**
	 * 向指定URL发送GET方法的请求
	 * 
	 * @param url
	 *            发送请求的URL
	 * 
	 * @return URL 所代表远程资源的响应结果
	 */
	public static String sendGet(String url) {
		StringBuilder resultSB = new StringBuilder();
		BufferedReader in = null;
		try {
			String urlNameString = url;
			URL realUrl = new URL(urlNameString);
			// 打开和URL之间的连接
			URLConnection connection = realUrl.openConnection();
			// 设置通用的请求属性
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 建立实际的连接
			connection.connect();
			// 定义 BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				resultSB.append(line);
			}
		} catch (Exception e) {
			LOG.error("URLConnection Exception", e);
		}
		// 使用finally块来关闭输入流
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				LOG.error("close connection exception", e2);
			}
		}
		return resultSB.toString();
	}

	public String match(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return matcher.group(1);
		}

		return null;
	}

}
