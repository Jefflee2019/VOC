package com.bfd.parse.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;

public class UrlNormalizerUtil {

	public static String getHost(String url) {
		try {
			return new URL(url).getHost().toLowerCase();
		} catch (MalformedURLException e) {
			return null;
		}
	}

	public static String getPath(String url) {
		try {
			return new URL(url).getPath();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getURIPath(String url) {
		try {
			return new URI(url).getPath();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getQuery(String url) {
		try {
			return new URL(url).getQuery();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getURIQuery(String url) {
		try {
			return new URL(url).getQuery();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static int getPort(String url) {
		try {
			return new URL(url).getPort();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return -1;
		}
	}

	public static String getProtocol(String url) {
		try {
			return new URL(url).getProtocol();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String normalize(String url, String baseUrl) {
		if (StringUtils.isEmpty(url)) {
			return url;
		}
		StringBuilder res = new StringBuilder();

		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			String bHost = getHost(baseUrl);
			int biPort = getPort(baseUrl);
			String bPort = (biPort == 80 || biPort == -1) ? "" : String.valueOf(biPort);
			String bProtocol = getProtocol(baseUrl);
			res.append(bProtocol).append("://").append(bHost);
			if (StringUtils.isNotEmpty(bPort)) {
				res.append(":").append(bPort);
			}
			if (url.startsWith("/")) {
				res.append(url);
			} else {
				String bPath = getPath(baseUrl);
				if (StringUtils.isNotEmpty(bPath)) {
					bPath = bPath.substring(0, bPath.lastIndexOf("/") + 1);
				}
				res.append("/").append(bPath).append(url);
			}
		} else {
			StringBuilder turl = new StringBuilder();
			int iport = getPort(url);
			String path = getPath(url);
			String query = getQuery(url);
			String host = getHost(url);
			String protocol = getProtocol(url);
			String port = (iport == 80 || iport == -1) ? "" : String.valueOf(iport);
			if (StringUtils.isNotEmpty(protocol)) {
				turl.append(protocol).append("://");
			}
			if (StringUtils.isNotEmpty(host)) {
				turl.append(host);
			}
			if (StringUtils.isNotEmpty(port)) {
				turl.append(":").append(port);
			}
			if (StringUtils.isNotEmpty(path)) {
				turl.append("/").append(path);
			}
			if (StringUtils.isNotEmpty(query)) {
				turl.append("?").append(query);
			}
			res.append(turl);
		}
		try {
			return new URI(res.toString()).normalize().toString();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return res.toString();
	}

	public static void main(String[] args) {

		// System.out.println(getPath("http://www.baidu.com/dsfs/sdfsdf/dsfdf.do?dkf=sdf&dd=d"));

		String[][] urlss = {
				{ "http://imgs.i.kadang.cn/imgs/d/1/admin/120704/0918/g-sUwVrkBJbsib9-jBPcH.jpg.image?type=large",
						"http://www.kadang.com/diy/detail3891.html" },
				{ "http://kadang.i.kadang.cn:80/kadang/web/images/px.gif", "http://www.kadang.com/diy/detail3685.html" },
				{ "http://www.baidu.com:8010/ddd/dd.do?dddddd=23&ddd=eekdkd--23kkd---kkd--d.html#dd",
						"http://www.baidu.com:80/a/b/c.do?id=2&dd#dd" },
				{ "/ddd/sss.do?ddd&ddd=2#sd", "http://www.baidu.com/dsdf/a/b/" },
				{ "../ddd/sss?ddd==", "http://www.baidu.com/dsdf/sss/" } };
		for (String[] urls : urlss) {
			System.out.println(normalize(urls[0], urls[1]));
		}
	}
}
