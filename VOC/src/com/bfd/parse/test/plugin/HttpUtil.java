package com.bfd.parse.test.plugin;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class HttpUtil {
	private static final String ua = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.101 Safari/537.36";

	private String url = "";
	private String charset = "";
	private Map<String, String> headers = null;
	private String ip = "";
	private Map<String, String> posts = null;
	private boolean isGet = true;
	private String referer = "";

	public HttpUtil(String url, String charset, String referer, Map<String, String> headers, String ip, Map<String, String> posts,
			boolean isGet) {
		this.url = url;
		this.charset = charset;
		this.headers = headers;
		this.ip = ip;
		this.posts = posts;
		this.isGet = isGet;
		this.referer = referer;
	}

	public String excute() throws Exception {
		if (isGet) {
			return httpGet(url, charset, referer, headers, ip);
		} else {
			return httpPost(url, charset, referer, headers, posts, ip);
		}
	}

	/**
	 * get 请求示例代码
	 * 
	 * @param url
	 * @param charset
	 * @param headers
	 * @return
	 * @throws Exception
	 */
	public String httpGet(String url, String charset, String referer, Map<String, String> headers, String ip) throws Exception {
		HttpClientBuilder httpBuilder = HttpClientBuilder.create();
		httpBuilder.setUserAgent(ua);

		HttpClient client = httpBuilder.build();
		HttpGet httpget = new HttpGet(url);

		if (null != ip && !"".equals(ip.trim())) {
			RequestConfig requestConfig = getRequestConfigByIp(ip);
			if (null != requestConfig) {
				httpget.setConfig(requestConfig);
			}
		}

		if (referer != null && referer.length() > 0) {
			httpget.setHeader("Referer", referer);
		}

		if (headers != null && headers.size() > 0) {
			for (String key : headers.keySet()) {
				httpget.setHeader(key, headers.get(key));
			}
		}
		HttpResponse response = client.execute(httpget);
		HttpEntity en = response.getEntity();
		String content = EntityUtils.toString(en, (charset == null || "".equals(charset)) ? "utf8" : charset);
		return content;
	}

	/**
	 * post 请求示例代码
	 * 
	 * @param url
	 * @param charset
	 * @param headers
	 * @param posts
	 * @param ip
	 * @return
	 * @throws Exception
	 */
	public String httpPost(String url, String charset, String referer, Map<String, String> headers, Map<String, String> posts, String ip)
			throws Exception {
		HttpClientBuilder httpBuilder = HttpClientBuilder.create();
		httpBuilder.setUserAgent(ua);
		HttpClient client = httpBuilder.build();
		HttpPost httppost = new HttpPost(url);

		if (null != ip && !"".equals(ip)) {
			RequestConfig requestConfig = getRequestConfigByIp(ip);
			if (null != requestConfig) {
				httppost.setConfig(requestConfig);
			}
		}

		if (referer != null && referer.length() > 0) {
			httppost.setHeader("Referer", referer);
		}

		if (headers != null && headers.size() > 0) {
			for (String key : headers.keySet()) {
				httppost.setHeader(key, headers.get(key));
			}
		}

		List<NameValuePair> postList = new ArrayList<NameValuePair>();
		if (posts != null && posts.size() > 0) {
			for (String key : posts.keySet()) {
				postList.add(new BasicNameValuePair(key, posts.get(key)));
			}
			UrlEncodedFormEntity postEntity = new UrlEncodedFormEntity(postList);
			httppost.setEntity(postEntity);
		}

		HttpResponse response = client.execute(httppost);
		HttpEntity en = response.getEntity();
		String content = EntityUtils.toString(en, (charset == null || "".equals(charset)) ? "utf8" : charset);
		return content;
	}

	/**
	 * 指定ip
	 * 
	 * @param ip
	 * @return
	 */
	public RequestConfig getRequestConfigByIp(String ip) {

		RequestConfig requestConfig = null;
		try {
			RequestConfig.Builder config_builder = RequestConfig.custom();
			InetAddress inetAddress = null;
			inetAddress = InetAddress.getByName(ip);
			config_builder.setLocalAddress(inetAddress);
			requestConfig = config_builder.build();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return requestConfig;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Map<String, String> getPosts() {
		return posts;
	}

	public void setPosts(Map<String, String> posts) {
		this.posts = posts;
	}

	public boolean isGet() {
		return isGet;
	}

	public void setGet(boolean isGet) {
		this.isGet = isGet;
	}

	public static String getUa() {
		return ua;
	}

	public String getReferer() {
		return referer;
	}

	public void setReferer(String referer) {
		this.referer = referer;
	}
}
