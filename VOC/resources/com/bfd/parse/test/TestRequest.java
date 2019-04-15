package com.bfd.parse.test;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.DataUtil;
import com.bfd.crawler.utils.EncodeUtil;
import com.bfd.parse.util.JsonUtil;

public class TestRequest {
	private static final Log LOG = LogFactory.getLog(TestRequest.class);

	private byte[] page;
	private String data;
	private String pageData;
	private String encode;
	private String cid;
	private String url;
	private String testerName;
	private Map<String, Object> reqMap;

	public TestRequest(byte[] page, String data, String pageData, String encode, String cid, String url,
			String testerName, Map<String, Object> reqMap) {
		this.page = page;
		this.data = data;
		this.pageData = pageData;
		this.encode = encode;
		this.cid = cid;
		this.url = url;
		this.reqMap = reqMap;
		this.testerName = testerName;
	}

	public static TestRequest create(String pageData, String param) {
		try {
			if (StringUtils.isNotEmpty(pageData) && StringUtils.isNotEmpty(param)) {
				Map<String, Object> reqMap = (Map<String, Object>) JsonUtil.parseObject(param);
				//testType为
				String testerName = (String) reqMap.get("testType");
				if (StringUtils.isEmpty(testerName)) {
					LOG.info("Invalid request, testType is empty.");
					return null;
				}
				String cid = (String) reqMap.get("cid");
				String url = (String) reqMap.get("url");
				byte[] bytes = DataUtil.unzipAndDecode(pageData);
				String encode = EncodeUtil.getHtmlEncode(bytes);
				String data = new String(bytes, encode);
				return new TestRequest(bytes, data, pageData, encode, cid, url, testerName, reqMap);
			}
		} catch (Exception e) {
			LOG.info("parse test request exception", e);
		}
		return null;
	}

	public byte[] getPage() {
		return page;
	}

	public void setPage(byte[] page) {
		this.page = page;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Map<String, Object> getReqMap() {
		if (reqMap == null) {
			reqMap = new HashMap<String, Object>();
		}
		return reqMap;
	}

	public void setResMap(Map<String, Object> reqMap) {
		this.reqMap = reqMap;
	}

	public String getData() {
		return data;
	}

	public String getEncode() {
		return encode;
	}

	public void setData(String data) {
		this.data = data;
	}

	public void setEncode(String encode) {
		this.encode = encode;
	}

	public String getTesterName() {
		return testerName;
	}

	public void setTesterName(String testerName) {
		this.testerName = testerName;
	}

	public String getPageData() {
		return pageData;
	}

	public void setPageData(String pageData) {
		this.pageData = pageData;
	}
}
