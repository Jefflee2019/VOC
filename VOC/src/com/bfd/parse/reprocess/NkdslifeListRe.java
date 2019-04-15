package com.bfd.parse.reprocess;

import java.io.*;
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
import com.bfd.parse.util.ParseUtils;
import com.bfd.parse.entity.Constants;

/**
 * 站点名：Nkdslife
 * 
 * 功能：列表页加上下一页
 * 
 * @author bfd_06
 */
public class NkdslifeListRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NkdslifeListRe.class);
	
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		/**
		 * 将tasks内的link替换为重定向后的link
		 */
		if (resultData != null && resultData.containsKey(Constants.TASKS)) {
			List<Map<String, Object>> tasksList = (List<Map<String, Object>>) resultData
					.get(Constants.TASKS);
			if (!tasksList.isEmpty()) {
				for (Map<String, Object> tempMap : tasksList) {
					if (tempMap.containsKey("link")) {
						String link = tempMap.get("link").toString();
						Pattern patten = Pattern.compile("http.+html");
						Matcher matcher = patten.matcher(sendGet(link));
						if (matcher.find()) {
							link = matcher.group();
						}
						tempMap.put("link", link);
					}
				}
			}
		}
		/**
		 * 添加下一页
		 */
		if (resultData.containsKey(Constants.ITEMS)) {
			List<Map<String, Object>> items = (List<Map<String, Object>>) resultData
					.get(Constants.ITEMS);
			// 如果列表页全满则加上下一页
			if (items.size() >= 15) {
				String pageNum = match("list_0_(\\d+)_", unit.getUrl());
				String keyName = match("_([^_]+).html$", unit.getUrl());
				if (pageNum != null && keyName != null) {
					List<Map<String, Object>> rtasks = (List<Map<String, Object>>) resultData
							.get(Constants.TASKS);
					Map<String, Object> rtask = new HashMap<String, Object>();
					String nextUrl = "http://article.kdslife.com/list_0_%s_%s.html";
					nextUrl = String.format(nextUrl,
							Integer.parseInt(pageNum) + 1, keyName);
					rtask.put("link", nextUrl);
					rtask.put("rawlink", nextUrl);
					rtask.put("linktype", "newslist");
					resultData.put(Constants.NEXTPAGE, nextUrl);
					rtasks.add(rtask);
				}
			}
		}
		ParseUtils.getIid(unit, result);

		return new ReProcessResult(processcode, processdata);
	}

	public String match(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
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
		String result = "";
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
				result += line;
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
		return result;
	}
	
}
