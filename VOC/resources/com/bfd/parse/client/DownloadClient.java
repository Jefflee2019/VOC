package com.bfd.parse.client;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import Ice.ObjectPrx;

import com.bfd.crawler.DownloaderPrx;
import com.bfd.crawler.DownloaderPrxHelper;
import com.bfd.crawler.utils.ConfigUtils;
import com.bfd.crawler.utils.DataUtil;
import com.bfd.crawler.utils.EncodeUtil;
import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.test.weibosinaparser.WeiboParser;

public class DownloadClient extends AbstractClient {

	private static final Log LOG = LogFactory.getLog(DownloadClient.class);

	public DownloadClient() {
		super();
	}

	@Override
	public String name() {
		return "downloadservice";
	}

	@Override
	protected ObjectPrx checkedCast() {
		return DownloaderPrxHelper.checkedCast(base);
	}

	@Override
	protected String getProxyConfig() {
		return ConfigUtils.getInstance().getProp("Downloader.Proxy", "DownloadService");
	}

	@Override
	protected DownloaderPrx getService() {
		return ((DownloaderPrx) super.getService());
	}

	public String getPage(String url) {
		Map<String, Object> req = new HashMap<String, Object>();
		req.put("url", url);
		req.put("cid", "C1905");
		url = JsonUtils.toJSONString(req);
		try {
			return getService().getOnePage(url);
		} catch (Exception e) {
			LOG.warn(Thread.currentThread().getName() + " exception while calling getPage , err:", e);
		}
		long l1 = 1000, l2 = 1000, lmax = 120000, ltemp;
		while (true) {
			try {
				Thread.sleep(l1);
				ltemp = l1;
				l1 = l2;
				l2 = ltemp + l2;
				if (l2 >= lmax) {
					l1 = l2 = 1000;
				}
				if (init())
					return getService().getOnePage(url);
			} catch (InterruptedException x) {
			} catch (Exception x) {
				LOG.warn(Thread.currentThread().getName() + " exception while connect to downloader server: " + l1
						/ 1000 + ", " + l2 / 1000 + "", x);
			}
			LOG.debug(Thread.currentThread().getName() + " try to connect to downloader server: " + l1 / 1000 + ", "
					+ l2 / 1000);
		}
	}

	public String getPageData(String url,String isAjax,String cid,String type
			,String refer,String cookie,String ip) {
		try {
			LOG.info(Thread.currentThread().getName()+" download client geting page, url=" + url);
			
			int httpcode=996 ;
			int retry = 0 ;
			while(httpcode == 996){
				String result = getPage(url,isAjax,cid,type,refer,cookie,ip);
				Map<String, Object> resMap = (Map<String, Object>) JsonUtils.parseObject(result);
				Map<String, Object> spider = (Map<String, Object>) resMap.get("spiderdata");
				httpcode = (Integer) spider.get("httpcode") ;
				LOG.info(new StringBuffer().append(Thread.currentThread().getName()).append(" httcode=").append(httpcode).append(" , retry=").append(retry).append(" , url=").append(url).toString());
				if (httpcode == 200) {
					String pagedata = (String) spider.get("data");
					String charset = (String) spider.get("charset");
					byte[] bytes = DataUtil.unzipAndDecode(pagedata);
					String encode = EncodeUtil.getHtmlEncode(bytes, charset);
					LOG.info(Thread.currentThread().getName()+" download client  got page data");
					return new String(bytes, encode);
				}
				retry++ ;
			}
		} catch (Exception e) {
			LOG.warn("Download page data error, ", e);
		}
		return null;
	}
	
//	public String getPage(String url,String cid,String refer,String cookie) {
//		Map<String, Object> req = new HashMap<String, Object>();
//		req.put("url", url);
//		req.put("cid", cid);
//		req.put("referer", refer);
//		if(cookie!=null&&cookie.trim().length()!=0){
//			req.put("cookie", cookie);
//		}
//		url = JsonUtil.toJSONString(req);
//		try {
//			return getService().getPage(url);
//		} catch (Exception e) {
//			LOG.warn(Thread.currentThread().getName() + " exception while calling getPage , err:", e);
//		}
//		long l1 = 1000, l2 = 1000, lmax = 120000, ltemp;
//		while (true) {
//			try {
//				Thread.sleep(l1);
//				ltemp = l1;
//				l1 = l2;
//				l2 = ltemp + l2;
//				if (l2 >= lmax) {
//					l1 = l2 = 1000;
//				}
////				destroy() ;
//				init() ;
//					return getService().getPage(url);
//			} catch (InterruptedException x) {
//			} catch (Exception x) {
//				LOG.warn(Thread.currentThread().getName() + " exception while connect to downloader server: " + l1
//						/ 1000 + ", " + l2 / 1000 + "", x);
//			}
//			LOG.debug(Thread.currentThread().getName() + " try to connect to downloader server: " + l1 / 1000 + ", "
//					+ l2 / 1000);
//		}
//	}
	/**
	 * 1.url;2.jsajax;3.cid;4.type(item,list);5.refer;6.cookie,7.ip,8.ajax_pagefield,9.ajaxext
	 * @param URL
	 * @param isajax
	 * @param cid
	 * @param type
	 * @return
	 */
	public String getPage(Object... params) {
		if(params==null||params.length==0){
			return "";
		}
		
		String[] paramKey = {"url","needajaxdata","cid","type","refer","cookie","pagetype","siteid","ip","ajax_page_field","ajaxext"};
		
		Map<String, Object> req = new HashMap<String, Object>();
		Map<String, Object> accdata = new HashMap<String, Object>();
//		accdata.put("loginip", "");
		accdata.put("userid", "13412022647");
		accdata.put("code", "0");
		accdata.put("siteid", "35");
		for(int i=0;i<params.length;i++){
			if(params[i].toString().trim().length()!=0){
				if(paramKey[i].equalsIgnoreCase("cookie")){
					accdata.put("cookie", params[i]);
				}else{
					req.put(paramKey[i], params[i]);
				}
				
			}
		}
		req.put("accdata", accdata);
		req.put("type", "test");
//		req.put("url", url);
//		req.put("isajax", isajax);
//		req.put("cid", cid);
//		req.put("type", type);
		String requestJson = JsonUtils.toJSONString(req);
		LOG.info("requestjson:"+requestJson);
		try {
			return getService().getOnePage(requestJson);
		} catch (Exception e) {
			LOG.warn(Thread.currentThread().getName() + " exception while calling getPage , err:", e);
		}
		long l1 = 1000, l2 = 1000, lmax = 120000, ltemp;
		while (true) {
			try {
				Thread.sleep(l1);
				ltemp = l1;
				l1 = l2;
				l2 = ltemp + l2;
				if (l2 >= lmax) {
					l1 = l2 = 1000;
				}
				if (init())
					return getService().getOnePage(requestJson);
			} catch (InterruptedException x) {
			} catch (Exception x) {
				LOG.warn(Thread.currentThread().getName() + " exception while connect to downloader server: " + l1
						/ 1000 + ", " + l2 / 1000 + "", x);
			}
			LOG.debug(Thread.currentThread().getName() + " try to connect to downloader server: " + l1 / 1000 + ", "
					+ l2 / 1000);
		}
	}

	/**
	 * 压缩编码后的页面内容
	 * 
	 * @param testurl
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String getPageData(String url) {
		try {
			LOG.info("download client geting page, url=" + url);
			String result = getPage(url);
			LOG.info("download client  got page data");
			Map<String, Object> resMap = (Map<String, Object>) JsonUtils.parseObject(result);
			Map<String, Object> spider = (Map<String, Object>) resMap.get("spiderdata");
			if ((Integer) spider.get("httpcode") == 200) {
				String pagedata = (String) spider.get("data");
				String charset = (String) spider.get("charset");
				byte[] bytes = DataUtil.unzipAndDecode(pagedata);
				String encode = EncodeUtil.getHtmlEncode(bytes, charset);
				return new String(bytes, encode);
			}
		} catch (Exception e) {
			LOG.warn("Download page data error, ", e);
		}
		return null;
	}

	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");
		String url0 = "http://weixin.sogou.com/weixin?query=%E5%8D%8E%E4%B8%BA&fr=sgsearch&type=2&ie=utf8&w=01019900&sut=1918&sst0=1441706539107&lkt=0%2C0%2C0";
		String url = "http://weixin.sogou.com/websearch/art.jsp?sg=CBf80b2xkgZdlrlP83m3xgEMeacTVcpEF8KWIvA4_n51cyNNyejYkZHXbnWRKDp6gGRCdYYSc_4rujf9h8Jhg4Y7sr8Rlk4YwfqCEUR7aTQAcHp4KQJgrM0bHQX1RR6d&url=p0OVDH8R4SHyUySb8E88hkJm8GF_McJfBfynRTbN8whQJg_0WPiWa9jGpcKi4811RD7iN2JSGUvIJyoW3Cfu5mQ3JxMQ3374wPGHHcB_Q_aS2cSBxZvpt-257UkqmhpJPRQzELLiAppYy-5x5In7jJFmExjqCxhpkyjFvwP6PuGcQ64lGQ2ZDMuqxplQrsbk";
		DownloadClient client = new DownloadClient();
//		String url = "http://weibo.com/3733532417/CpgJI89RK?type=comment";
		
		String stringZip0 = client.getPage(url0, "1", "sina", "test", "",
				"", "weibo", "35");
		
		String stringZip = client.getPage(url, "1", "sina", "test", "",
				"", "weibo", "35");
		
		Map<String, Object> resMap = null;
		try {
			resMap = (Map<String, Object>) JsonUtils.parseObject(stringZip);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		Map<String, Object> spider = (Map<String, Object>) resMap
				.get("spiderdata");
		
		String html = WeiboParser.getHtml(spider);
		System.out.println(html);
//		Pattern p = Pattern.compile("iframeContent = ([\\s\\S]*?)\"");
//		Matcher m  = p.matcher(page);
//		String fragment = "";
//		while(m.find()){
//			fragment = m.group(1);
//		}
//		try {
//			fragment = URLDecoder.decode(fragment, "utf-8");
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
	}
}
