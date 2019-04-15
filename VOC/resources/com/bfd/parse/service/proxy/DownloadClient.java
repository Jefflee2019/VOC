package com.bfd.parse.service.proxy;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Ice.ObjectPrx;

import com.bfd.crawler.DownloaderPrx;
import com.bfd.crawler.DownloaderPrxHelper;
import com.bfd.crawler.utils.ConfigUtils;
import com.bfd.crawler.utils.DataUtil;
import com.bfd.crawler.utils.EncodeUtil;
import com.bfd.parse.util.JsonUtil;

public class DownloadClient extends AbstractIceProxy {

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
		return ConfigUtils.getInstance().getProp("Downloader.Proxy", "DownonepageService");
	}

	@Override
	protected DownloaderPrx getService() {
		return ((DownloaderPrx) super.getService());
	}

	public String getPage(String url) {
		Map<String, Object> req = new HashMap<String, Object>();
		req.put("url", url);
		url = JsonUtil.toJSONString(req);
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
			Map<String, Object> resMap = (Map<String, Object>) JsonUtil.parseObject(result);
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
}
