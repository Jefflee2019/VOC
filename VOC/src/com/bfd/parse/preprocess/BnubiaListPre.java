package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

public class BnubiaListPre implements PreProcessor {

	private static final Log LOG = LogFactory.getLog(BnubiaListPre.class);

	@Override
	public boolean process(ParseUnit unit, ParserFace face) {
		String pageData = unit.getPageData();
		pageData = gethtml(unit.getUrl());
		unit.setPageData(pageData);
		try {
			unit.setPageBytes(pageData.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			LOG.error("BxiciPostPre preprocess error");
		}
		unit.setPageEncode("utf8");
		return true;
	}
	
	/**
	 * 下载页面内容
	 */
	public static String gethtml (String url) {
		String htmlContent = "";
		HttpClientBuilder builder = HttpClientBuilder.create();
		builder.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.181 Safari/537.36");
		HttpClient client = builder.build();
		HttpGet request = new HttpGet(url);
		request.setHeader("Cookie", "SERVERID=dec20dfb3c28acd90b7b81b6520d0cba|1533024054|1533020640; "
				+ "aliyungf_tc=AQAAAF1R5wgQdwsALz1hcFg2RQs/ALlv; "
				+ "acw_tc=AQAAAIifAyptsgsALz1hcDTr/efqezM/; "
				+ "acw_sc__=5b60172bc37c2ebc1eb6a469ae48e5861be1b7ed");
		try {
			HttpResponse response = client.execute(request);
			htmlContent = EntityUtils.toString(response.getEntity(),"utf-8");
			System.err.println(htmlContent);
			return htmlContent;
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}

	
}
