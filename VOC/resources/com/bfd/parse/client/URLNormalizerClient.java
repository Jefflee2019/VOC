package com.bfd.parse.client;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import Ice.ObjectPrx;

import com.bfd.crawler.UrlHandlePrx;
import com.bfd.crawler.UrlHandlePrxHelper;
import com.bfd.crawler.utils.ConfigUtils;
import com.bfd.crawler.utils.JsonUtils;

public class URLNormalizerClient {

	private static final Log LOG = LogFactory.getLog(URLNormalizerClient.class);

	private static final Pattern imgFilerRegex = Pattern
			.compile(
					"^\\s*https?://([-0-9a-z]+\\.)+(com|org|net|[a-z]{2}|firm|store|Web|arts|rec|info|nom|edu)(\\.|:\\d+)?(/[^\\s'\"]*)?\\.(BMP|JPG|JPEG|PNG|GIF)(?![a-z])\\s*$",
					Pattern.CASE_INSENSITIVE);

	private static final Pattern urlRegex = Pattern
			.compile(
					"^\\s*https?://([-0-9a-z]+\\.)+(com|org|net|[a-z]{2}|firm|store|Web|arts|rec|info|nom|edu)(\\.|:\\d+)?(/[^\\s'\"]*)?\\s*$",
					Pattern.CASE_INSENSITIVE);

	public URLNormalizerClient() {
		super();
	}


	public String normalize(String bizName, String type, String url, String baseurl, boolean bNormalize) {
		return this.normalize(bizName, type, url, baseurl, bNormalize, false);
	}

	public String normalize(String bizName, String type, String url, String baseurl, boolean bNormalize,
			boolean filteImg) {
		// if (testMode) {
		// return url;
		// }
		if (StringUtils.isBlank(url)) {
			LOG.warn(Thread.currentThread().getName() + " Invalid url while normalizing, url=" + url + ", cid="
					+ bizName + ", baseurl=" + baseurl);
			return "";
		}
		Map<String, Object> resMap = normalizeExt(bizName, type, url, baseurl, bNormalize, filteImg);
		Integer code = (Integer) resMap.get("code");
		if (code == null || (code != 0 && code != 3)) {
			return "";
		}
		return (String) resMap.get("url");
	}

	public Map<String, Object> normalizeExt(String bizName, String type, String url, String baseurl,
			boolean bNormalize, boolean filteImg) {
		String res = normalizeE(bizName, type, url, baseurl, bNormalize, filteImg);
		Map<String, Object> resMap = null;
		try {
			resMap = (Map<String, Object>) JsonUtils.parseObject(res);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (resMap == null) {
			resMap = new HashMap<String, Object>();
			resMap.put("code", -1);
		}
		Integer code = (Integer) resMap.get("code");
		if (code == null || (code != 0 && code != 3)) {
			return resMap;
		}
		String resUrl = (String) resMap.get("url");
		boolean isMatch = true;
		if (filteImg)
			isMatch = imgFilerRegex.matcher(resUrl).find();
		else
			isMatch = urlRegex.matcher(resUrl).find();
		resMap.put("url", isMatch ? resUrl.trim() : "");
		return resMap;
	}

	public String normalizeE(String bizName, String type, String url, String baseurl, boolean bNormalize,
			boolean filteImg) {
		try {
			String fallUrl = getFallUrl(baseurl,url);
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("code", 0);
			map.put("url", fallUrl);
			return JsonUtils.toJSONString(map);
		} catch (Exception e) {
			LOG.warn(Thread.currentThread().getName() + " exception while calling normalize ,url=" + url + ", cid="
					+ bizName + ", baseurl=" + baseurl, e);
		}
		return "";
	}
	
	public static String getFallUrl(String parentURL, String url) {

		if (url == null) {
			return "";
		}
		
		if ("".equals(url)) {
			return "";
		}
		if (url.toLowerCase().startsWith("javascript")) {
			return "";
		}
		URL u = null;
		try {

			if (url.startsWith("?")) { // 子 HREF 值以 ? 开头
				if (parentURL.indexOf("?") == -1) {
					u = new URL(parentURL + url);
				} else if (parentURL.indexOf("?") != -1) {
					u = new URL(parentURL.substring(0, parentURL.indexOf("?")) + url);
				}
			} else if (url.startsWith("../")) { // 提取的 URL 值.
				if (StringUtils.countMatches(parentURL, "/") == 2) {
					u = new URL(new URL(parentURL), "/" + url.replace("../", ""));
				} else if (StringUtils.countMatches(parentURL, "/") == 3) {
					u = new URL(new URL(parentURL), url.replace("../", ""));
				} else {
					u = new URL(new URL(parentURL), url);
				}
			} else {
				u = new URL(new URL(parentURL), url);
			}

			String link = u.toExternalForm();
			if (link.startsWith("https://")) { // 不支持 https://
				return "";
			}
		} catch (Exception e) {
			
		}
		
		if (u == null) {
			return "";
		}
		String fallUrl = u.toString();
		if (fallUrl.contains("../")) {
			fallUrl = fallUrl.replace("../", "");
		}
		return fallUrl;
	}

	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");
		String url = "/dggde/6.jpg";
		String base = "http://www.jiazhao.com/news18156";
		URLNormalizerClient normalizer = new URLNormalizerClient();
		String normalize = normalizer.normalize("Cmaibaobao", "_img", url, base, true,true);
//		normalize(cid, type + "_img", srcLink, this.url, true, FilteImg);
		System.out.println(normalize);
	}
}
